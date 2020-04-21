package com.m3sv.plainupnp.upnp.manager


import com.m3sv.plainupnp.common.utils.formatTime
import com.m3sv.plainupnp.common.utils.throttle
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.upnp.*
import com.m3sv.plainupnp.upnp.didl.ClingAudioItem
import com.m3sv.plainupnp.upnp.didl.ClingDIDLContainer
import com.m3sv.plainupnp.upnp.didl.ClingImageItem
import com.m3sv.plainupnp.upnp.didl.ClingVideoItem
import com.m3sv.plainupnp.upnp.usecase.LaunchLocallyUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class UpnpManagerImpl @Inject constructor(
    private val renderer: RendererDiscoveryObservable,
    private val contentDirectory: ContentDirectoryDiscoveryObservable,
    private val serviceController: UpnpServiceController,
    private val launchLocallyUseCase: LaunchLocallyUseCase,
    private val stateStore: UpnpStateStore,
    upnpNavigator: UpnpNavigator
) : UpnpManager,
    CoroutineScope,
    UpnpNavigator by upnpNavigator {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()

    private val upnpInnerStateChannel = BroadcastChannel<UpnpRendererState>(Channel.CONFLATED)

    override val upnpRendererState: Flow<UpnpRendererState> = upnpInnerStateChannel.asFlow()

    private var upnpInnerState: UpnpInnerState? = null

    private var rendererCommand: RendererCommand? = null

    private var isLocal: Boolean = false

    private var next: Int = -1

    private var previous: Int = -1

    private val renderItem: BroadcastChannel<RenderItem> = BroadcastChannel(1)

    init {
        launch {
            renderItem
                .openSubscription()
                .throttle(scope = this)
                .collect { renderItem ->
                    render(renderItem)
                }
        }
    }

    override val contentDirectories: Flow<List<DeviceDisplay>> = contentDirectory.subscribe()

    override val renderers: Flow<List<DeviceDisplay>> = renderer.observe()

    override fun selectContentDirectory(position: Int) {
        if (position !in contentDirectory.currentContentDirectories.indices) {
            Timber.d("Content directory position is outside of bounds, ignore")
            navigateTo(Destination.Empty)
            serviceController.selectedContentDirectory = null
            return
        }

        val contentDirectory = contentDirectory.currentContentDirectories[position].device

        if (contentDirectory != serviceController.selectedContentDirectory) {
            serviceController.selectedContentDirectory = contentDirectory
            navigateTo(Destination.Home)
        }
    }

    override fun selectRenderer(position: Int) {
        if (position !in renderer.currentRenderers.indices) {
            Timber.d("Renderer position is outside of bounds, ignore")
            return
        }

        val renderer = renderer.currentRenderers[position].device

        if (renderer is LocalDevice) {
            isLocal = true
        } else {
            isLocal = false
            serviceController.selectedRenderer = renderer
        }
    }

    private fun renderItem(item: RenderItem) {
        launch {
            renderItem.send(item)
        }
    }

    private var currentRendererState: UpnpRendererState? = null

    private fun render(item: RenderItem) {
        Timber.d("Render item: ${item.item.uri}")

        rendererCommand?.run {
            pause()
            commandStop()
        }

        next = item.position + 1
        previous = item.position - 1

        if (isLocal) {
            launchLocallyUseCase.execute(item)
            return
        }

        upnpInnerState = with(item.item) {
            val type = when (this) {
                is ClingAudioItem -> UpnpItemType.AUDIO
                is ClingImageItem -> UpnpItemType.IMAGE
                is ClingVideoItem -> UpnpItemType.VIDEO
                else -> UpnpItemType.UNKNOWN
            }

            UpnpInnerState(id, uri, type)
        }

        upnpInnerState?.let { innerState ->
            launch {
                innerState.flow.collect { state ->
                    currentRendererState = state
                    upnpInnerStateChannel.offer(state)
                }
            }

            rendererCommand = serviceController
                .createRendererCommand(innerState)
                ?.apply {
                    if (item.item !is ClingImageItem)
                        resume()
                    launchItem(item.item)
                }

            currentRendererState = null
        }
    }

    override fun playNext() {
        launch {
            stateStore.peekState()?.let { state ->
                if (state is ContentState.Success
                    && next in state.upnpDirectory.content.indices
                    && state.upnpDirectory.content[next].didlObject is DIDLItem
                ) {
                    renderItem(
                        RenderItem(
                            state.upnpDirectory.content[next].didlObject as DIDLItem,
                            next
                        )
                    )
                }
            }
        }
    }

    override fun playPrevious() {
        launch {
            stateStore.peekState()?.let { state ->
                if (state is ContentState.Success
                    && previous in state.upnpDirectory.content.indices
                    && state.upnpDirectory.content[previous].didlObject is DIDLItem
                ) {
                    renderItem(
                        RenderItem(
                            state.upnpDirectory.content[previous].didlObject as DIDLItem,
                            previous
                        )
                    )
                }
            }
        }
    }

    override fun resumeRendererUpdate() {
        Timber.v("Resume renderer update")
        rendererCommand?.resume()
    }

    override fun pauseRendererUpdate() {
        Timber.v("Pause renderer update")
        rendererCommand?.pause()
    }

    override fun pausePlayback() {
        rendererCommand?.commandPause()
    }

    override fun stopPlayback() {
        rendererCommand?.commandStop()
    }

    override fun resumePlayback() {
        rendererCommand?.commandPlay()
    }

    override fun moveTo(progress: Int) {
        upnpInnerState?.run {
            rendererCommand?.commandSeek(formatTime(MAX_VOLUME_PROGRESS, progress, durationSeconds))
        }
    }

    override fun dispose() {
        coroutineContext.cancel()
    }

    override fun itemClick(position: Int) {
        launch {
            stateStore.peekState()?.let { state ->
                when (state) {
                    is ContentState.Success -> handleClick(position, state.upnpDirectory.content)
                    is ContentState.Loading -> {
                        // no-op
                    }
                }
            }
        }
    }

    private fun handleClick(position: Int, content: List<DIDLObjectDisplay>) {
        if (position in content.indices) {
            val item = content[position]

            when (item.didlObject) {
                is ClingDIDLContainer -> navigateTo(
                    Destination.Path(
                        item.didlObject.id,
                        item.title
                    )
                )

                else -> renderItem(
                    RenderItem(
                        content[position].didlObject as DIDLItem,
                        position
                    )
                )
            }
        }
    }

    override fun togglePlayback() {
        currentRendererState?.state?.let { state ->
            when (state) {
                UpnpRendererState.State.PLAY -> pausePlayback()
                UpnpRendererState.State.PAUSE -> resumePlayback()
                UpnpRendererState.State.STOP -> resumePlayback()
                UpnpRendererState.State.INITIALIZING,
                UpnpRendererState.State.FINISHED -> {
                }
            }
        }
    }

    private companion object {
        private const val MAX_VOLUME_PROGRESS = 100
    }
}

data class RenderItem(
    val item: DIDLItem,
    val position: Int
)

