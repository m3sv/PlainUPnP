package com.m3sv.plainupnp.upnp.manager


import com.m3sv.plainupnp.common.Consumable
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
import com.m3sv.plainupnp.upnp.store.ContentState
import com.m3sv.plainupnp.upnp.store.UpnpStateStore
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import com.m3sv.plainupnp.upnp.usecase.LaunchLocallyUseCase
import com.m3sv.plainupnp.upnp.util.duration
import com.m3sv.plainupnp.upnp.util.position
import com.m3sv.plainupnp.upnp.util.remainingDuration
import com.m3sv.plainupnp.upnp.volume.VolumeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.fourthline.cling.UpnpService
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.model.item.*
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val MAX_PROGRESS = 100

@ExperimentalCoroutinesApi
class UpnpManagerImpl @Inject constructor(
    private val upnpService: UpnpService,
    private val rendererDiscoveryObservable: RendererDiscoveryObservable,
    private val contentDirectoryObservable: ContentDirectoryDiscoveryObservable,
    private val launchLocallyUseCase: LaunchLocallyUseCase,
    private val stateStore: UpnpStateStore,
    private val database: Database,
    private val upnpRepository: UpnpRepository,
    private val volumeRepository: VolumeRepository,
    private val errorReporter: ErrorReporter,
    upnpNavigator: UpnpNavigator
) : UpnpManager,
    UpnpNavigator by upnpNavigator,
    CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private val upnpInnerStateChannel = BroadcastChannel<UpnpRendererState>(Channel.CONFLATED)

    override val upnpRendererState: Flow<UpnpRendererState> = upnpInnerStateChannel.asFlow()

    private var isLocal: Boolean = false

    private var currentPlayingIndex = -1

    override val contentDirectories: Flow<List<DeviceDisplay>> =
        contentDirectoryObservable.observe()

    override val renderers: Flow<List<DeviceDisplay>> = rendererDiscoveryObservable.observe()

    private val updateDispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    override val actionErrors: Flow<Consumable<String>> = errorReporter.errorFlow

    override fun selectContentDirectory(position: Int) {
        val contentDirectory = contentDirectoryObservable.currentContentDirectories[position].device

        database.selectedDeviceQueries.insertSelectedDevice(
            CONTENT_DIRECTORY_TYPE,
            contentDirectory.fullIdentity
        )

        contentDirectoryObservable.selectedContentDirectory = contentDirectory

        safeNavigateTo(ErrorReason.BROWSE_FAILED) { command, service ->
            navigateTo(
                destination = Destination.Home,
                contentDirectoryCommand = command,
                contentDirectoryService = service
            )
        }
    }

    override fun selectRenderer(position: Int) {
        val renderer = rendererDiscoveryObservable.currentRenderers[position].device

        isLocal = renderer is LocalDevice

        database.selectedDeviceQueries.insertSelectedDevice(
            RENDERER_TYPE,
            renderer.fullIdentity
        )

        if (!isLocal) {
            if (rendererDiscoveryObservable.selectedRenderer != renderer)
                stopUpdate()

            rendererDiscoveryObservable.selectedRenderer = renderer
        } else {
            stopUpdate()
        }
    }

    private suspend fun renderItem(item: RenderItem) {
        stopUpdate()

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

        safeAvAction { service ->
            upnpRepository.setUri(service, uri, trackMetadata)
            upnpRepository.play(service)
        }

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
            stopUpdate()
            safeAvAction { service ->
                updateJob = launch(updateDispatcher) {
                    while (isActive) {
                        delay(500)

                        val transportInfo = upnpRepository.getTransportInfo(service)
                        val positionInfo = upnpRepository.getPositionInfo(service)

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

    private fun stopUpdate() {
        updateJob?.cancel()
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
            safeAvAction { service -> upnpRepository.pause(service) }
        }
    }

    override fun stopPlayback() {
        launch {
            safeAvAction { service -> upnpRepository.stop(service) }
        }
    }

    override fun resumePlayback() {
        launch {
            safeAvAction { service -> upnpRepository.play(service) }
        }
    }

    override fun seekTo(progress: Int) {
        launch {
            safeAvAction { service ->
                pauseUpdate = true
                upnpRepository.seekTo(service, formatTime(MAX_PROGRESS, progress, currentDuration))
                pauseUpdate = false
            }
        }
    }

    override val volumeFlow: Flow<Int> = volumeRepository.volumeFlow

    override suspend fun raiseVolume(step: Int) {
        safeRcAction { service -> volumeRepository.raiseVolume(service, step) }
    }

    override suspend fun lowerVolume(step: Int) {
        safeRcAction { service -> volumeRepository.lowerVolume(service, step) }
    }

    override suspend fun muteVolume(mute: Boolean) {
        safeRcAction { service -> volumeRepository.muteVolume(service, mute) }
    }

    override suspend fun setVolume(volume: Int) {
        safeRcAction { service -> volumeRepository.setVolume(service, volume) }
    }

    override suspend fun getVolume(): Int = safeRcAction { service ->
        volumeRepository.getVolume(service)
    } ?: 0

    override fun itemClick(position: Int) {
        launch {
            currentPlayingIndex = position

            stateStore.getCurrentState()?.let { state ->
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
                is ClingDIDLContainer -> {
                    safeNavigateTo { command, service ->
                        val path = Destination.Path(
                            item.didlObject.id,
                            item.title
                        )

                        navigateTo(
                            destination = path,
                            contentDirectoryCommand = command,
                            contentDirectoryService = service
                        )
                    }
                }

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
            safeAvAction { service ->
                if (isPaused)
                    upnpRepository.play(service)
                else
                    upnpRepository.pause(service)
            }
        }
    }

    private inline fun safeNavigateTo(
        errorReason: ErrorReason? = null,
        block: (ContentDirectoryCommand, ClingService) -> Unit
    ) {
        val command = ContentDirectoryCommand(upnpService.controlPoint)

        contentDirectoryObservable.selectedContentDirectory?.let { selectedDevice ->
            val service: Service<*, *>? =
                (selectedDevice as CDevice).device.findService(UDAServiceType("ContentDirectory"))

            if (service != null && service.hasActions()) block(
                command,
                ClingService(service)
            ) else
                errorReason.report()
        }
    }

    private inline fun safeAvAction(
        errorReason: ErrorReason? = null,
        block: (Service<*, *>) -> Unit
    ) {
        rendererDiscoveryObservable.selectedRenderer?.let { renderer ->
            val service: Service<*, *>? =
                (renderer as CDevice).device.findService(UDAServiceType("AVTransport"))

            if (service != null && service.hasActions())
                block(service)
            else
                errorReason.report()
        }
    }

    private inline fun <T> safeRcAction(
        errorReason: ErrorReason? = null,
        block: (Service<*, *>) -> T
    ): T? {
        return rendererDiscoveryObservable.selectedRenderer?.let { renderer ->
            val service: Service<*, *>? =
                (renderer as CDevice).device.findService(UDAServiceType("RenderingControl"))

            if (service != null && service.hasActions()) block(service) else {
                errorReason.report()
                null
            }
        }
    }

    private fun ErrorReason?.report() {
        if (this != null) errorReporter.report(this)
    }
}

data class RenderItem(
    val didlItem: ClingDIDLObject,
    val position: Int
)

