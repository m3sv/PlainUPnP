package com.m3sv.plainupnp.upnp.manager


import com.m3sv.plainupnp.common.util.formatTime
import com.m3sv.plainupnp.core.persistence.CONTENT_DIRECTORY_TYPE
import com.m3sv.plainupnp.core.persistence.Database
import com.m3sv.plainupnp.core.persistence.RENDERER_TYPE
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.LocalDevice
import com.m3sv.plainupnp.data.upnp.UpnpItemType
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.upnp.*
import com.m3sv.plainupnp.upnp.didl.ClingDIDLContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.discovery.device.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.discovery.device.RendererDiscoveryObservable
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import com.m3sv.plainupnp.upnp.usecase.LaunchLocallyUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.model.item.*
import timber.log.Timber
import java.time.Duration
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

@ExperimentalCoroutinesApi
class UpnpManagerImpl @Inject constructor(
    private val renderer: RendererDiscoveryObservable,
    private val contentDirectory: ContentDirectoryDiscoveryObservable,
    private val serviceController: UpnpServiceController,
    private val launchLocallyUseCase: LaunchLocallyUseCase,
    private val stateStore: UpnpStateStore,
    private val database: Database,
    private val upnpRepository: UpnpRepository,
    upnpNavigator: UpnpNavigator
) : UpnpManager,
    UpnpNavigator by upnpNavigator,
    CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

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

        database.selectedDeviceQueries.insertSelectedDevice(
            CONTENT_DIRECTORY_TYPE,
            contentDirectory.fullIdentity
        )

        serviceController.selectedContentDirectory = contentDirectory
        navigateTo(Destination.Home)
    }

    override fun selectRenderer(position: Int) {
        if (position !in renderer.currentRenderers.indices) {
            serviceController.selectedRenderer = null
            return
        }

        val renderer = renderer.currentRenderers[position].device

        isLocal = renderer is LocalDevice

        database.selectedDeviceQueries.insertSelectedDevice(
            RENDERER_TYPE,
            renderer.fullIdentity
        )

        if (!isLocal) {
            serviceController.selectedRenderer = renderer
        }
    }

    private suspend fun renderItem(item: RenderItem) {
        if (isLocal) {
            launchLocallyUseCase.execute(item)
            return
        }

        val didlItem = item.didlItem.didlObject as Item

        val uri = item.didlItem.didlObject.firstResource?.value ?: return

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

        upnpRepository.setUri(uri, trackMetadata)
        upnpRepository.play()

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
        } else {
            with(didlItem) {
                showImageInfo(
                    id = id,
                    uri = uri,
                    title = title
                )
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
            updateJob = launch(updateDispatcher) {
                while (isActive) {
                    delay(500)

                    val transportInfo = upnpRepository.getTransportInfo()
                    val positionInfo = upnpRepository.getPositionInfo()

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

    private fun showImageInfo(
        id: String,
        uri: String,
        title: String
    ) {
        val state = UpnpRendererState(
            id = id,
            uri = uri,
            type = UpnpItemType.IMAGE,
            state = TransportState.STOPPED,
            remainingDuration = null,
            duration = null,
            position = null,
            elapsedPercent = null,
            durationSeconds = null,
            title = title,
            artist = null
        )

        upnpInnerStateChannel.offer(state)
    }

    override fun playNext() {
        val newPosition = currentPlayingIndex + 1
        itemClick(newPosition)
    }

    override fun playPrevious() {
        val newPosition = currentPlayingIndex - 1
        itemClick(newPosition)
    }

    override fun pausePlayback() {
        launch {
            upnpRepository.pause()
        }
    }

    override fun stopPlayback() {
        launch {
            upnpRepository.stop()
        }
    }

    override fun resumePlayback() {
        launch {
            upnpRepository.play()
        }
    }

    override fun seekTo(progress: Int) {
        launch {
            pauseUpdate = true
            upnpRepository.seekTo(formatTime(MAX_PROGRESS, progress, currentDuration))
            pauseUpdate = false
        }
    }

    override fun itemClick(position: Int) {
        launch {
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
    }

    private suspend fun handleClick(position: Int, content: List<ClingDIDLObject>) {
        if (position in content.indices) {
            when (val item = content[position]) {
                is ClingDIDLContainer -> navigateTo(
                    Destination.Path(
                        item.didlObject.id,
                        item.title
                    )
                )

                else -> renderItem(
                    RenderItem(
                        content[position],
                        position
                    )
                )
            }
        }
    }

    override fun togglePlayback() {
        launch {
            if (isPaused)
                upnpRepository.play()
            else
                upnpRepository.pause()
        }
    }

    private companion object {
        private const val MAX_PROGRESS = 100
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
    val didlItem: ClingDIDLObject,
    val position: Int
)

