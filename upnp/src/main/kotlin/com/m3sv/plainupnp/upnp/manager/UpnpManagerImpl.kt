package com.m3sv.plainupnp.upnp.manager


import com.m3sv.plainupnp.common.utils.formatTime
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.upnp.*
import com.m3sv.plainupnp.upnp.actions.*
import com.m3sv.plainupnp.upnp.didl.ClingDIDLContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLItem
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import com.m3sv.plainupnp.upnp.usecase.LaunchLocallyUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.model.item.*
import timber.log.Timber
import java.time.Duration
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.abs

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
    private val getTransportInfo: GetTransportInfoAction,
    private val getPositionInfo: GetPositionInfoAction,
    private val avServiceFinder: AvServiceFinder,
    upnpNavigator: UpnpNavigator
) : UpnpManager,
    UpnpNavigator by upnpNavigator {

    private val upnpInnerStateChannel = BroadcastChannel<UpnpRendererState>(Channel.CONFLATED)

    override val upnpRendererState: Flow<UpnpRendererState> = upnpInnerStateChannel.asFlow()

    private var isLocal: Boolean = false

    private var currentPlayingIndex = -1

    override val contentDirectories: Flow<List<DeviceDisplay>> = contentDirectory.subscribe()

    override val renderers: Flow<List<DeviceDisplay>> = renderer.observe()

    private val updateDispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    override fun selectContentDirectory(position: Int) {
        if (position !in contentDirectory.currentContentDirectories.indices) {
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
            serviceController.selectedRenderer = null
            return
        }

        val renderer = renderer.currentRenderers[position].device

        isLocal = renderer is LocalDevice

        if (!isLocal) {
            serviceController.selectedRenderer = renderer
        }
    }

    private suspend fun renderItem(item: RenderItem) {
        if (isLocal) {
            launchLocallyUseCase.execute(item)
            return
        }

        val didlItem = (item.didlItem as ClingDIDLItem).didlObject as Item

        val uri = item.didlItem.uri ?: return

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

        executeWithAvService {
            setUri(this, uri, trackMetadata)
            play(this)

            if (didlItem is AudioItem || didlItem is VideoItem) {
                with(didlItem) {
                    launchUpdate(
                        id = id,
                        uri = uri,
                        type = when (this) {
                            is AudioItem -> UpnpItemType.AUDIO
                            is ImageItem -> UpnpItemType.IMAGE
                            is VideoItem -> UpnpItemType.VIDEO
                            else -> UpnpItemType.UNKNOWN
                        },
                        title = title,
                        artist = creator
                    )
                }
            }
        }
    }

    private var currentDuration: Long = 0L

    private var pauseUpdate = false

    private var isPaused = false

    private var updateJob: Job? = null

    private suspend fun launchUpdate(
        id: String,
        uri: String?,
        type: UpnpItemType,
        title: String,
        artist: String?
    ) {
        withContext(Dispatchers.IO) {
            updateJob?.cancel()
            executeWithAvService {
                updateJob = launch(updateDispatcher) {
                    while (isActive) {
                        delay(500)

                        val transportInfo = getTransportInfo(this@executeWithAvService)
                        val positionInfo = getPositionInfo(this@executeWithAvService)

                        if (transportInfo == null || positionInfo == null) {
                            break
                        }

                        isPaused =
                            transportInfo.currentTransportState == TransportState.PAUSED_PLAYBACK

                        val state = UpnpRendererState(
                            id = id,
                            uri = uri,
                            type = type,
                            state = transportInfo.currentTransportState,
                            remainingDuration = positionInfo.remainingDuration,
                            duration = positionInfo.duration,
                            position = positionInfo.position,
                            elapsedPercent = positionInfo.elapsedPercent,
                            durationSeconds = positionInfo.trackDurationSeconds,
                            title = title,
                            artist = artist ?: ""
                        )

                        currentDuration = positionInfo.trackDurationSeconds

                        if (!pauseUpdate)
                            upnpInnerStateChannel.offer(state)

                        Timber.d("Got new state: $state")

                        if (transportInfo.currentTransportState == TransportState.STOPPED)
                            break
                    }
                }
            }
        }
    }

    override suspend fun playNext() {
        val newPosition = currentPlayingIndex + 1
        itemClick(newPosition)
    }

    override suspend fun playPrevious() {
        val newPosition = currentPlayingIndex - 1
        itemClick(newPosition)
    }

    override suspend fun pausePlayback() {
        executeWithAvService { pause(this) }
    }

    override suspend fun stopPlayback() {
        executeWithAvService { stop(this) }
    }

    override suspend fun resumePlayback() {
        executeWithAvService { play(this) }
    }

    override suspend fun seekTo(progress: Int) {
        executeWithAvService {
            pauseUpdate = true

            seekTo(this, formatTime(MAX_VOLUME_PROGRESS, progress, currentDuration))
            delay(1000)
            pauseUpdate = false
        }
    }

    override suspend fun itemClick(position: Int) {
        currentPlayingIndex = position

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
        executeWithAvService {
            if (isPaused) {
                play(this)
            } else {
                pause(this)
            }
        }
    }

    private companion object {
        private const val MAX_VOLUME_PROGRESS = 100
    }

    private inline fun executeWithAvService(block: Service<*, *>.() -> Unit) {
        val avService = avServiceFinder.getService()
        if (avService != null) {
            block(avService)
        }
    }
}

private inline val PositionInfo.remainingDuration: String
    get() {
        val duration = Duration.ofSeconds(trackRemainingSeconds)
        val seconds = duration.seconds
        val absSeconds = abs(seconds)
        val positive = "%d:%02d:%02d".format(
            absSeconds / 3600,
            (absSeconds % 3600) / 60,
            absSeconds % 60
        )

        val sign = if (seconds < 0) "-" else ""

        return "$sign$positive"
    }

private inline val PositionInfo.duration: String
    get() {
        val duration = Duration.ofSeconds(trackDurationSeconds)
        val seconds = duration.seconds
        val absSeconds = abs(seconds)
        val positive = "%d:%02d:%02d".format(
            absSeconds / 3600,
            (absSeconds % 3600) / 60,
            absSeconds % 60
        )

        val sign = if (seconds < 0) "-" else ""

        return "$sign$positive"
    }

private inline val PositionInfo.position: String
    get() {
        val duration = Duration.ofSeconds(trackElapsedSeconds)
        val seconds = duration.seconds
        val absSeconds = abs(seconds)
        val positive = "%d:%02d:%02d".format(
            absSeconds / 3600,
            (absSeconds % 3600) / 60,
            absSeconds % 60
        )

        val sign = if (seconds < 0) "-" else ""

        return "$sign$positive"
    }

data class RenderItem(
    val didlItem: DIDLItem,
    val position: Int
)

