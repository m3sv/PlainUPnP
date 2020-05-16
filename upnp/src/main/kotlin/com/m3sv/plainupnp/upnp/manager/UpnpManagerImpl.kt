package com.m3sv.plainupnp.upnp.manager


import com.m3sv.plainupnp.common.utils.formatTime
import com.m3sv.plainupnp.common.utils.throttle
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.data.upnp.EmptyUpnpRendererState.durationSeconds
import com.m3sv.plainupnp.upnp.*
import com.m3sv.plainupnp.upnp.actions.*
import com.m3sv.plainupnp.upnp.didl.ClingDIDLContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLItem
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import com.m3sv.plainupnp.upnp.usecase.LaunchLocallyUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.fourthline.cling.support.model.item.*
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
class UpnpManagerImpl @Inject constructor(
    private val renderer: RendererDiscoveryObservable,
    private val contentDirectory: ContentDirectoryDiscoveryObservable,
    private val serviceController: UpnpServiceController,
    private val launchLocallyUseCase: LaunchLocallyUseCase,
    private val stateStore: UpnpStateStore,
    private val stop: StopAction,
    private val pause: PauseAction,
    private val play: PlayAction,
    private val setUri: SetUriAction,
    private val seekTo: SeekAction,
    upnpNavigator: UpnpNavigator
) : UpnpManager,
    UpnpNavigator by upnpNavigator {

    private val upnpInnerStateChannel = BroadcastChannel<UpnpRendererState>(Channel.CONFLATED)

    override val upnpRendererState: Flow<UpnpRendererState> = upnpInnerStateChannel.asFlow()

    private var isLocal: Boolean = false

    private var next: Int = -1

    private var previous: Int = -1

    private val renderItem: BroadcastChannel<RenderItem> = BroadcastChannel(1)

    init {
        GlobalScope.launch {
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

    private suspend fun renderItem(item: RenderItem) {
        renderItem.send(item)
    }

    private var currentRendererState: UpnpRendererState? = null

    private suspend fun render(renderItem: RenderItem) {
        if (isLocal) {
            launchLocallyUseCase.execute(renderItem)
            return
        }

        next = renderItem.position + 1
        previous = renderItem.position - 1

        val didlItem = (renderItem.didlItem as ClingDIDLItem).didlObject as Item
        val uri = renderItem.didlItem.uri ?: return
        val type = when (didlItem) {
            is AudioItem -> "audioItem"
            is VideoItem -> "videoItem"
            is ImageItem -> "imageItem"
            is PlaylistItem -> "playlistItem"
            is TextItem -> "textItem"
            else -> return
        }

        // TODO genre && artURI
        val trackMetadata = with(didlItem) {
            TrackMetadata(
                id,
                title,
                creator,
                "",
                "",
                firstResource.value,
                "object.item.$type"
            )
        }

        setUri(uri, trackMetadata)
        play()
    }

    override suspend fun playNext() {
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

    override suspend fun playPrevious() {
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


    override suspend fun pausePlayback() {
        pause()
    }

    override suspend fun stopPlayback() {
        stop()
    }

    override suspend fun resumePlayback() {
        play()
    }

    override suspend fun seekTo(progress: Int) {
        seekTo(formatTime(MAX_VOLUME_PROGRESS, progress, durationSeconds))
    }

    override suspend fun itemClick(position: Int) {
        stateStore.peekState()?.let { state ->
            when (state) {
                is ContentState.Success -> handleClick(position, state.upnpDirectory.content)
                is ContentState.Loading -> {
                    // no-op
                }
            }
        }
    }

    private suspend fun handleClick(position: Int, content: List<DIDLObjectDisplay>) {
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

    override suspend fun togglePlayback() {
        currentRendererState?.state?.let { state ->
            when (state) {
                UpnpRendererState.State.PLAY -> pausePlayback()
                UpnpRendererState.State.PAUSE -> resumePlayback()
                UpnpRendererState.State.STOP -> resumePlayback()
                UpnpRendererState.State.INITIALIZING,
                UpnpRendererState.State.FINISHED -> Unit
            }
        }
    }

    private companion object {
        private const val MAX_VOLUME_PROGRESS = 100
    }
}

data class RenderItem(
    val didlItem: DIDLItem,
    val position: Int
)

