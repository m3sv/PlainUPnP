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
                clearBackStack()
                browse(BrowseToModel(HOME_DIRECTORY_ID, HOME_DIRECTORY_NAME))
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
        }
    }


    private fun browse(model: BrowseToModel) {
        serviceController.createContentDirectoryCommand()?.browse(model.id, null) {
            val successState = ContentState.Success(model.directoryName, it ?: listOf())

            addCurrentStateToBackStack()

            currentState = successState
            setContentState(successState)
        }
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
        directories.clear()
    }

    private companion object {
        private const val HOME_DIRECTORY_ID = "0"
        private const val HOME_DIRECTORY_NAME = "Home"

    }
}
