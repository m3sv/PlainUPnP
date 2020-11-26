//package com.m3sv.plainupnp.upnp
//
//import com.m3sv.plainupnp.upnp.store.ContentState
//import com.m3sv.plainupnp.upnp.store.UpnpDirectory
//import com.m3sv.plainupnp.upnp.store.UpnpStateStore
//import org.fourthline.cling.model.meta.Service
//import java.util.*
//import javax.inject.Inject
//
//interface UpnpNavigator {
//    fun navigateTo(
//        destination: Destination,
//        contentDirectoryService: ClingService?
//    )
//}
//
//sealed class Destination {
//    object Home : Destination()
//    object Back : Destination()
//    object Empty : Destination()
//    data class Path(
//        val id: String,
//        val directoryName: String
//    ) : Destination()
//}
//
//data class BrowseToModel(
//    val id: String,
//    val directoryName: String
//)
//
//class UpnpNavigatorImpl @Inject constructor(private val stateStore: UpnpStateStore) :
//    UpnpNavigator {
//
//    private var directories = Stack<ContentState.Success>()
//
//    override fun navigateTo(
//        destination: Destination,
//        contentDirectoryService: ClingService?
//    ) {
//        when (destination) {
//            is Destination.Home -> {
//                browse(
//                    requireNotNull(contentDirectoryService).service,
//                    BrowseToModel(HOME_DIRECTORY_ID, HOME_DIRECTORY_NAME),
//                    clearBackStack = true
//                )
//            }
//
//            is Destination.Path -> {
//                browse(
//                    requireNotNull(contentDirectoryService).service,
//                    BrowseToModel(destination.id, destination.directoryName)
//                )
//            }
//
//            is Destination.Back -> {
//                if (directories.isNotEmpty()) {
//                    val directory = directories.pop()
//                    currentState = directory
//                    setContentState(directory)
//                }
//            }
//
//            is Destination.Empty -> browseToEmptyState()
//        }
//    }
//
//    private fun browse(
//        contentDirectoryService: Service<*, *>?,
//        model: BrowseToModel,
//        clearBackStack: Boolean = false
//    ) {
//        contentDirectoryCommand.browse(contentDirectoryService, model.id, null) { directories ->
//            val directory = if (model.id == HOME_DIRECTORY_ID) {
//                UpnpDirectory.Root(HOME_DIRECTORY_NAME, directories ?: listOf())
//            } else {
//                UpnpDirectory.SubUpnpDirectory(model.directoryName, directories ?: listOf())
//            }
//
//            val state = ContentState.Success(directory)
//
//            if (clearBackStack)
//                clearBackStack()
//            else
//                addCurrentStateToBackStack()
//
//            currentState = state
//            setContentState(state)
//        }
//    }
//
//    private fun browseToEmptyState() {
//        val state = ContentState.Success(UpnpDirectory.None)
//
//        clearBackStack()
//        currentState = state
//        setContentState(state)
//    }
//
//    private fun setContentState(state: ContentState) {
//        stateStore.setState(state)
//    }
//
//    private fun addCurrentStateToBackStack() {
//        if (currentState != null) directories.push(currentState)
//    }
//
//    private fun clearBackStack() {
//        currentState = null
//        directories.clear()
//    }
//}
