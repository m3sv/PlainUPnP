package com.m3sv.plainupnp.upnp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface UpnpNavigator {
    fun navigateTo(destination: Destination)
}

sealed class Destination {
    object Home : Destination()
    object Back : Destination()
    object Empty : Destination()
    data class Path(val id: String, val directoryName: String) : Destination()
}

data class BrowseToModel(
    val id: String,
    val directoryName: String
)

class UpnpNavigatorImpl @Inject constructor(
    private val serviceController: UpnpServiceController,
    private val stateStore: UpnpStateStore
) : UpnpNavigator, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()

    private var directories = Stack<ContentState.Success>()

    private var currentState: ContentState.Success? = null

    override fun navigateTo(destination: Destination) {
        when (destination) {
            is Destination.Home -> {
                setContentState(ContentState.Loading)
                browse(BrowseToModel(HOME_DIRECTORY_ID, HOME_DIRECTORY_NAME), clearBackStack = true)
            }

            is Destination.Path -> {
                setContentState(ContentState.Loading)
                browse(BrowseToModel(destination.id, destination.directoryName))
            }

            is Destination.Back -> {
                if (directories.isNotEmpty()) {
                    val directory = directories.pop()
                    currentState = directory
                    setContentState(directory)
                }
            }

            is Destination.Empty -> browseToEmptyState()
        }
    }

    private fun browse(model: BrowseToModel, clearBackStack: Boolean = false) {
        serviceController.createContentDirectoryCommand()?.browse(model.id, null) { directories ->
            val directory = if (model.id == HOME_DIRECTORY_ID) {
                UpnpDirectory.Root(HOME_DIRECTORY_NAME, directories ?: listOf())
            } else {
                UpnpDirectory.SubUpnpDirectory(model.directoryName, directories ?: listOf())
            }

            val state = ContentState.Success(directory)

            if (clearBackStack)
                clearBackStack()
            else
                addCurrentStateToBackStack()

            currentState = state
            setContentState(state)
        }
    }

    private fun browseToEmptyState() {
        val state = ContentState.Success(UpnpDirectory.None)

        clearBackStack()
        currentState = state
        setContentState(state)
    }

    private fun setContentState(state: ContentState) {
        launch {
            stateStore.setState(state)
        }
    }

    private fun addCurrentStateToBackStack() {
        if (currentState != null) directories.push(currentState)
    }

    private fun clearBackStack() {
        currentState = null
        directories.clear()
    }

    private companion object {
        private const val HOME_DIRECTORY_ID = "0"
        private const val HOME_DIRECTORY_NAME = "Home"

    }
}
