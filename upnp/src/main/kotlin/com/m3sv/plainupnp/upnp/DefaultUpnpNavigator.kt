package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.common.utils.throttle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.Future
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class DefaultUpnpNavigator @Inject constructor(
        private val factory: UpnpFactory,
        private val upnpStateRepository: UpnpStateStore)
    : UpnpNavigator, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()

    private val browseTo: Channel<BrowseToModel> = Channel()

    private var directoriesStructure = Stack<ContentState>()

    private var browseFuture: Future<*>? = null

    private var previousState: ContentState? = null

    init {
        launch {
            browseTo.throttle(scope = this).collect { model ->
                navigate(model)
            }
        }
    }

    override fun navigateTo(destination: Destination) {
        browseFuture?.cancel(true)

        when (destination) {
            is Destination.Home -> {
                setContentState(ContentState.Loading)
                directoriesStructure.clear()
                previousState = null
                browseTo.offer(BrowseToModel("0", "Home"))
            }

            is Destination.Path -> {
                setContentState(ContentState.Loading)
                browseTo.offer(BrowseToModel(destination.id, destination.directoryName))
            }

            is Destination.Back -> {
                when {
                    directoriesStructure.size == 1 -> {
                        previousState = directoriesStructure.pop()
                        previousState?.let(this::setContentState)
                    }
                    directoriesStructure.size > 1 -> setContentState(directoriesStructure.pop())
                }
            }
        }
    }

    private fun navigate(model: BrowseToModel) {
        Timber.d("Browse: ${model.id}")

        browseFuture = factory.createContentDirectoryCommand()?.browse(model.id, null) {
            previousState?.let(directoriesStructure::push)
            val successState = ContentState.Success(model.directoryName, it ?: listOf())

            setContentState(successState)
            previousState = successState
        }
    }

    private fun setContentState(state: ContentState) {
        launch {
            upnpStateRepository.setState(state)
        }
    }
}