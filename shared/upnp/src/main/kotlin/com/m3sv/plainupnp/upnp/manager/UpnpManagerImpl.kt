package com.m3sv.plainupnp.upnp.manager


import com.m3sv.plainupnp.common.Consumable
import com.m3sv.plainupnp.common.util.formatTime
import com.m3sv.plainupnp.core.persistence.Database
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.upnp.CDevice
import com.m3sv.plainupnp.upnp.ContentUpdateState
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl
import com.m3sv.plainupnp.upnp.UpnpRepository
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.discovery.device.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.discovery.device.RendererDiscoveryObservable
import com.m3sv.plainupnp.upnp.folder.Folder
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import com.m3sv.plainupnp.upnp.usecase.LaunchLocallyUseCase
import com.m3sv.plainupnp.upnp.util.*
import com.m3sv.plainupnp.upnp.volume.VolumeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.scan
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.model.item.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

private const val MAX_PROGRESS = 100
private const val ROOT_FOLDER_ID = "0"
private const val AV_TRANSPORT = "AVTransport"
private const val RENDERING_CONTROL = "RenderingControl"
private const val CONTENT_DIRECTORY = "ContentDirectory"

data class PlayItem(
    val clingDIDLObject: ClingDIDLObject,
    val listIterator: ListIterator<ClingDIDLObject>,
)

sealed class Result {
    object Success : Result()
    object Error : Result()
}

class UpnpManagerImpl @Inject constructor(
    private val rendererDiscoveryObservable: RendererDiscoveryObservable,
    private val contentDirectoryObservable: ContentDirectoryDiscoveryObservable,
    private val launchLocally: LaunchLocallyUseCase,
    private val database: Database,
    private val upnpRepository: UpnpRepository,
    private val volumeRepository: VolumeRepository,
    private val errorReporter: ErrorReporter,
    private val contentRepository: UpnpContentRepositoryImpl,
) : UpnpManager,
    CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private var isLocal: Boolean = false
    private val upnpInnerStateChannel = MutableSharedFlow<UpnpRendererState>()
    override val upnpRendererState: Flow<UpnpRendererState> = upnpInnerStateChannel
    override val contentDirectories: Flow<List<DeviceDisplay>> = contentDirectoryObservable()
    override val renderers: Flow<List<DeviceDisplay>> = rendererDiscoveryObservable()
    override val actionErrors: Flow<Consumable<String>> = errorReporter.errorFlow

    private val folderChange = MutableSharedFlow<Folder>(1)
    override val folderChangeFlow: Flow<Folder> = folderChange

    private val updateChannel = MutableSharedFlow<Pair<Item, Service<*, *>>>()

    override val isContentDirectorySelected: Boolean
        get() = contentDirectoryObservable.selectedContentDirectory != null

    override val isConnectedToRender: Flow<UpnpDevice?>
        get() = rendererDiscoveryObservable.observeSelectRenderer()

    init {
        launch {
            updateChannel.scan(launch { }) { accumulator, pair ->
                accumulator.cancel()

                Timber.d("update: received new pair: ${pair.first}");
                val didlItem = pair.first
                val service = pair.second

                val type = when (didlItem) {
                    is AudioItem -> UpnpItemType.AUDIO
                    is VideoItem -> UpnpItemType.VIDEO
                    else -> UpnpItemType.UNKNOWN
                }

                val title = didlItem.title
                val artist = didlItem.creator
                val uri = didlItem.firstResource?.value ?: error("no uri")

                launch {
                    while (isActive) {
                        delay(500)

                        val transportInfo =
                            upnpRepository.getTransportInfo(service) ?: break

                        val positionInfo =
                            upnpRepository.getPositionInfo(service) ?: break

                        remotePaused =
                            transportInfo.currentTransportState == TransportState.PAUSED_PLAYBACK

                        val state = UpnpRendererState(
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

                        if (!pauseUpdate) upnpInnerStateChannel.emit(state)

                        Timber.d("Got new state: $state")

                        if (transportInfo.currentTransportState == TransportState.STOPPED) break
                    }
                }
            }.collect()
        }

        launch {
            contentRepository.updateState.collect {
                if (it is ContentUpdateState.Ready) {
                    val contentDirectory = contentDirectoryObservable.selectedContentDirectory

                    if (contentDirectory != null) {
                        safeNavigateTo(
                            errorReason = ErrorReason.BROWSE_FAILED,
                            folderId = ROOT_FOLDER_ID,
                            folderName = contentDirectory.friendlyName
                        )
                    }
                }
            }
        }
    }

    override fun selectContentDirectory(position: Int) {
        launch {
            val contentDirectory = contentDirectoryObservable
                .currentContentDirectories[position]
                .upnpDevice

            database
                .selectedDeviceQueries
                .insertSelectedDevice(CONTENT_DIRECTORY_TYPE, contentDirectory.fullIdentity)

            contentDirectoryObservable.selectedContentDirectory = contentDirectory

            safeNavigateTo(
                errorReason = ErrorReason.BROWSE_FAILED,
                folderId = ROOT_FOLDER_ID,
                folderName = contentDirectory.friendlyName
            )
        }
    }

    override fun selectContentDirectoryAsync(upnpDevice: UpnpDevice): Deferred<Result> = async {
        database
            .selectedDeviceQueries
            .insertSelectedDevice(CONTENT_DIRECTORY_TYPE, upnpDevice.fullIdentity)

        contentDirectoryObservable.selectedContentDirectory = upnpDevice

        safeNavigateTo(
            errorReason = ErrorReason.BROWSE_FAILED,
            folderId = ROOT_FOLDER_ID,
            folderName = upnpDevice.friendlyName
        )
    }

    override fun selectRenderer(position: Int) {
        launch {
            val renderer = rendererDiscoveryObservable.currentRenderers[position].upnpDevice

            isLocal = renderer is LocalDevice

            database
                .selectedDeviceQueries
                .insertSelectedDevice(RENDERER_TYPE, renderer.fullIdentity)

            if (isLocal || renderer != rendererDiscoveryObservable.getSelectedRenderer())
                stopUpdate()

            if (!isLocal) {
                rendererDiscoveryObservable.selectRenderer(renderer)
            } else {
                rendererDiscoveryObservable.selectRenderer(null)
            }
        }
    }

    private suspend fun renderItem(item: RenderItem) {
        stopUpdate()

        if (isLocal) {
            launchLocally(item)
            return
        }

        try {
            safeAvAction { service ->
                val didlItem = item.didlItem.didlObject as Item
                val uri = didlItem.firstResource?.value ?: return
                val didlType = didlType(didlItem)

                with(upnpRepository) {
                    setUri(service, uri, newMetadata(didlItem, didlType))
                    play(service)
                }

                when (didlItem) {
                    is AudioItem,
                    is VideoItem,
                    -> {
                        updateChannel.emit(didlItem to service)
                    }
                    is ImageItem -> {
                        with(didlItem) {
                            showImageInfo(
                                uri = uri,
                                title = title
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun newMetadata(
        didlItem: Item,
        didlType: String?,
    ): TrackMetadata = with(didlItem) {
        // TODO genre && artURI
        TrackMetadata(
            id,
            title,
            creator,
            "",
            "",
            firstResource.value,
            "object.item.$didlType"
        )
    }

    private fun didlType(didlItem: Item) = when (didlItem) {
        is AudioItem -> "audioItem"
        is VideoItem -> "videoItem"
        is ImageItem -> "imageItem"
        is PlaylistItem -> "playlistItem"
        is TextItem -> "textItem"
        else -> null
    }

    private var currentDuration: Long = 0L

    private var pauseUpdate = false

    private var remotePaused = false

    private fun stopUpdate() {
    }

    private suspend fun showImageInfo(
        uri: String,
        title: String,
    ) {
        val state = UpnpRendererState(
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

        upnpInnerStateChannel.emit(state)
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
        playItem: PlayItem,
    ) {
        launch {
            renderItem(RenderItem(playItem.clingDIDLObject))
            mediaIterator = playItem.listIterator
        }
    }

    override fun openFolder(folder: Folder) {
        launch {
            safeNavigateTo(
                errorReason = ErrorReason.BROWSE_FAILED,
                folderId = folder.id,
                folderName = folder.title
            )
        }
    }

    override fun togglePlayback() {
        launch {
            safeAvAction { service ->
                if (remotePaused)
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

    private suspend inline fun safeNavigateTo(
        errorReason: ErrorReason? = null,
        folderId: String,
        folderName: String,
    ): Result = contentDirectoryObservable.selectedContentDirectory
        ?.let { selectedDevice ->
            val service: Service<*, *>? =
                (selectedDevice as CDevice).device.findService(UDAServiceType(CONTENT_DIRECTORY))

            if (service == null || !service.hasActions()) {
                errorReason.report()
                return Result.Error
            }

            currentContent = upnpRepository.browse(service, folderId)
            currentFolderName = folderName.replace(UpnpContentRepositoryImpl.USER_DEFINED_PREFIX, "")

            val folder = when (folderId) {
                ROOT_FOLDER_ID -> Folder.Root(folderId, currentFolderName)
                else -> Folder.SubFolder(folderId, currentFolderName)
            }

            folderChange.emit(folder)
            Result.Success
        } ?: Result.Error

    private suspend inline fun safeAvAction(
        errorReason: ErrorReason? = null,
        block: (Service<*, *>) -> Unit,
    ) {
        rendererDiscoveryObservable.getSelectedRenderer()?.let { renderer ->
            val service: Service<*, *>? =
                (renderer as CDevice).device.findService(UDAServiceType(AV_TRANSPORT))

            if (service != null && service.hasActions())
                block(service)
            else
                errorReason.report()
        }
    }

    private suspend inline fun <T> safeRcAction(
        errorReason: ErrorReason? = null,
        block: (Service<*, *>) -> T,
    ): T? {
        return rendererDiscoveryObservable.getSelectedRenderer()?.let { renderer ->
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

    private suspend fun ErrorReason?.report() {
        if (this != null) errorReporter.report(this)
    }
}

inline class RenderItem(val didlItem: ClingDIDLObject)

