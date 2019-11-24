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

class UpnpNavigatorImpl @Inject constructor(
    private val serviceController: UpnpServiceController,
    private val stateStore: UpnpStateStore
) : UpnpNavigator, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()

    private var directoriesStructure = Stack<ContentState.Success>()

    override fun navigateTo(destination: Destination) {
        when (destination) {
            is Destination.Home -> {
                setContentState(ContentState.Loading)
                directoriesStructure.clear()
                browse(BrowseToModel("0", "Home"))
            }

            is Destination.Path -> {
                setContentState(ContentState.Loading)
                browse(BrowseToModel(destination.id, destination.directoryName))
            }

            is Destination.Back -> {
                if (!directoriesStructure.empty()) {
                    val directory = directoriesStructure.pop()

                    val state = if (directoriesStructure.empty()) {
                        directoriesStructure.push(directory)
                        ContentState.Exit(directory)
                    } else {
                        directoriesStructure.peek()
                    }

                    setContentState(state)
                }
            }
        }
    }

    private fun browse(model: BrowseToModel) {
        serviceController.createContentDirectoryCommand()?.browse(model.id, null) {
            val successState = ContentState.Success(model.directoryName, it ?: listOf())

            directoriesStructure.push(successState)

            setContentState(successState)
        }
    }

    private fun setContentState(state: ContentState) {
        launch {
            stateStore.setState(state)
        }
    }
}
