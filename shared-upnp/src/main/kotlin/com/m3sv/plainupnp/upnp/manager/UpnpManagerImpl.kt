package com.m3sv.plainupnp.upnp.manager


import com.m3sv.plainupnp.common.Consumable
import com.m3sv.plainupnp.common.util.formatTime
import com.m3sv.plainupnp.core.persistence.CONTENT_DIRECTORY_TYPE
import com.m3sv.plainupnp.core.persistence.Database
import com.m3sv.plainupnp.core.persistence.RENDERER_TYPE
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.upnp.CDevice
import com.m3sv.plainupnp.upnp.UpnpRepository
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.discovery.device.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.discovery.device.RendererDiscoveryObservable
import com.m3sv.plainupnp.upnp.folder.FolderType
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.model.item.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val MAX_PROGRESS = 100
private const val ROOT_FOLDER_ID = "0"
private const val AV_TRANSPORT = "AVTransport"
private const val RENDERING_CONTROL = "RenderingControl"
private const val CONTENT_DIRECTORY = "ContentDirectory"

@OptIn(
    ExperimentalCoroutinesApi::class,
    FlowPreview::class
)
class UpnpManagerImpl @Inject constructor(
    private val rendererDiscoveryObservable: RendererDiscoveryObservable,
    private val contentDirectoryObservable: ContentDirectoryDiscoveryObservable,
    private val launchLocally: LaunchLocallyUseCase,
    private val database: Database,
    private val upnpRepository: UpnpRepository,
    private val volumeRepository: VolumeRepository,
    private val errorReporter: ErrorReporter
) : UpnpManager,
    CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private val upnpInnerStateChannel = BroadcastChannel<UpnpRendererState>(Channel.CONFLATED)

    override val upnpRendererState: Flow<UpnpRendererState> = upnpInnerStateChannel.asFlow()

    private var isLocal: Boolean = false

    override val contentDirectories: Flow<List<DeviceDisplay>> =
        contentDirectoryObservable.observe()

    override val renderers: Flow<List<DeviceDisplay>> = rendererDiscoveryObservable.observe()

    override val actionErrors: Flow<Consumable<String>> = errorReporter.errorFlow

    private val folderChange: MutableStateFlow<Consumable<FolderType>> =
        MutableStateFlow(Consumable())

    override val folderChangeFlow: StateFlow<Consumable<FolderType>> = folderChange

    private val mutableFolderStructure = MutableStateFlow<List<FolderType>>(listOf())

    override val folderStructureFlow: Flow<List<FolderType>> = mutableFolderStructure

    private val folderStructure: Deque<FolderType> = LinkedList<FolderType>()

    override fun selectContentDirectory(position: Int) {
        val contentDirectory = contentDirectoryObservable
            .currentContentDirectories[position]
            .device

        saveSelectedContentDirectory(contentDirectory)

        contentDirectoryObservable.selectedContentDirectory = contentDirectory

        launch {
            safeNavigateTo(
                errorReason = ErrorReason.BROWSE_FAILED,
                folderId = ROOT_FOLDER_ID,
                folderName = contentDirectory.friendlyName
            )
        }
    }

    override fun selectRenderer(position: Int) {
        val renderer = rendererDiscoveryObservable.currentRenderers[position].device

        isLocal = renderer is LocalDevice

        saveSelectedRenderer(renderer)

        if (isLocal || renderer != rendererDiscoveryObservable.selectedRenderer)
            stopUpdate()

        if (!isLocal) {
            rendererDiscoveryObservable.selectedRenderer = renderer
        }
    }

    private suspend fun renderItem(item: RenderItem) {
        stopUpdate()

        if (isLocal) {
            launchLocally(item)
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
            with(upnpRepository) {
                setUri(service, uri, trackMetadata)
                play(service)
            }
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
            safeAvAction { service ->
                updateJob = launch {
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

                        if (!pauseUpdate) upnpInnerStateChannel.offer(state)

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
        launch {
            if (mediaIterator.hasNext()) {
                renderItem(RenderItem(mediaIterator.next()))
            }
        }
    }

    override fun playPrevious() {
        launch {
            if (mediaIterator.hasPrevious()) {
                renderItem(RenderItem(mediaIterator.previous()))
            }
        }
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
                upnpRepository.seekTo(
                    service = service,
                    time = formatTime(
                        max = MAX_PROGRESS,
                        progress = progress,
                        duration = currentDuration
                    )
                )
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

    override fun playItem(
        clingDIDLObject: ClingDIDLObject,
        listIterator: ListIterator<ClingDIDLObject>
    ) {
        launch {
            renderItem(RenderItem(clingDIDLObject))
            mediaIterator = listIterator
        }
    }

    override fun navigateTo(folderId: String, title: String) {
        launch {
            safeNavigateTo(
                errorReason = ErrorReason.BROWSE_FAILED,
                folderId = folderId,
                folderName = title
            )
        }
    }

    override fun navigateBack() {
        folderStructure.pollLast()
        updateFolderStructure()
    }

    private fun updateFolderStructure() {
        mutableFolderStructure.value = folderStructure.toList()
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

    private var currentContent = listOf<ClingDIDLObject>()

    private var currentFolderName: String = ""

    private var mediaIterator: ListIterator<ClingDIDLObject> =
        emptyList<ClingDIDLObject>().listIterator()

    override fun getCurrentFolderContents(): List<ClingDIDLObject> = currentContent

    override fun getCurrentFolderName(): String = currentFolderName

    private fun saveSelectedContentDirectory(contentDirectory: UpnpDevice) {
        database
            .selectedDeviceQueries
            .insertSelectedDevice(CONTENT_DIRECTORY_TYPE, contentDirectory.fullIdentity)
    }

    private fun saveSelectedRenderer(renderer: UpnpDevice) {
        database
            .selectedDeviceQueries
            .insertSelectedDevice(RENDERER_TYPE, renderer.fullIdentity)
    }

    private suspend inline fun safeNavigateTo(
        errorReason: ErrorReason? = null,
        folderId: String,
        folderName: String
    ) {
        contentDirectoryObservable.selectedContentDirectory?.let { selectedDevice ->
            val service: Service<*, *>? =
                (selectedDevice as CDevice).device.findService(UDAServiceType(CONTENT_DIRECTORY))

            if (service != null && service.hasActions()) {
                currentContent = upnpRepository.browse(service, folderId)
                currentFolderName = folderName

                val folder = when (folderId) {
                    ROOT_FOLDER_ID -> {
                        folderStructure.clear()
                        FolderType.Root(folderId, currentFolderName)
                    }
                    else -> FolderType.SubFolder(folderId, currentFolderName)
                }

                folderStructure.add(folder)

                folderChange.value = Consumable(folder)
                updateFolderStructure()
            } else
                errorReason.report()
        }
    }

    private inline fun safeAvAction(
        errorReason: ErrorReason? = null,
        block: (Service<*, *>) -> Unit
    ) {
        rendererDiscoveryObservable.selectedRenderer?.let { renderer ->
            val service: Service<*, *>? =
                (renderer as CDevice).device.findService(UDAServiceType(AV_TRANSPORT))

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
                (renderer as CDevice).device.findService(UDAServiceType(RENDERING_CONTROL))

            if (service != null && service.hasActions())
                block(service)
            else {
                errorReason.report()
                null
            }
        }
    }

    private fun ErrorReason?.report() {
        if (this != null) errorReporter.report(this)
    }
}

inline class RenderItem(val didlItem: ClingDIDLObject)

