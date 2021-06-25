package com.m3sv.plainupnp.upnp.manager


import com.m3sv.plainupnp.common.util.formatTime
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.data.upnp.UpnpItemType
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.logging.Log
import com.m3sv.plainupnp.presentation.SpinnerItem
import com.m3sv.plainupnp.upnp.CDevice
import com.m3sv.plainupnp.upnp.ContentUpdateState
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl
import com.m3sv.plainupnp.upnp.UpnpRepository
import com.m3sv.plainupnp.upnp.didl.ClingContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.didl.ClingMedia
import com.m3sv.plainupnp.upnp.didl.MiscItem
import com.m3sv.plainupnp.upnp.discovery.device.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.discovery.device.RendererDiscoveryObservable
import com.m3sv.plainupnp.upnp.folder.Folder
import com.m3sv.plainupnp.upnp.folder.FolderModel
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import com.m3sv.plainupnp.upnp.usecase.LaunchLocallyUseCase
import com.m3sv.plainupnp.upnp.util.*
import com.m3sv.plainupnp.upnp.volume.VolumeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
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

sealed class Result {
    object Success : Result()
    object Error : Result()
}

class UpnpManagerImpl @Inject constructor(
    private val rendererDiscoveryObservable: RendererDiscoveryObservable,
    private val contentDirectoryObservable: ContentDirectoryDiscoveryObservable,
    private val launchLocally: LaunchLocallyUseCase,
    private val upnpRepository: UpnpRepository,
    private val volumeRepository: VolumeRepository,
    private val contentRepository: UpnpContentRepositoryImpl,
    private val log: Log
) : UpnpManager,
    CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Default

    private var isLocal: Boolean = false

    private val upnpInnerStateChannel = MutableSharedFlow<UpnpRendererState>()

    override val upnpRendererState: Flow<UpnpRendererState> = upnpInnerStateChannel
    override val contentDirectories: Flow<List<DeviceDisplay>> = contentDirectoryObservable()
    override val renderers: Flow<List<DeviceDisplay>> = rendererDiscoveryObservable()

    private val updateChannel = MutableSharedFlow<Pair<Item, Service<*, *>>?>()

    override val isConnectedToRenderer: Flow<Boolean>
        get() = rendererDiscoveryObservable.observeSelectRenderer().map { it != null }

    override val volumeFlow: Flow<Int> = volumeRepository.volumeFlow

    init {
        launch {
            updateChannel.scan(launch { }) { accumulator, pair ->
                accumulator.cancel()

                if (pair == null)
                    return@scan launch { }

                Timber.d("update: received new pair: ${pair.first}")
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

                        val transportInfoAsync = async { upnpRepository.getTransportInfo(service) }
                        val positionInfoAsync = async { upnpRepository.getPositionInfo(service) }

                        combineResults(
                            transportInfoAsync.await(),
                            positionInfoAsync.await()
                        ) { transportInfo, positionInfo ->
                            remotePaused =
                                transportInfo.currentTransportState == TransportState.PAUSED_PLAYBACK

                            val state = UpnpRendererState.Default(
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

                            if (transportInfo.currentTransportState == TransportState.STOPPED) {
                                upnpInnerStateChannel.emit(UpnpRendererState.Empty)
                                cancel()
                            }
                        }
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
                            folderId = ROOT_FOLDER_ID,
                            folderName = contentDirectory.friendlyName
                        )
                    }
                }
            }
        }
    }

    private inline fun combineResults(
        transportInfo: TransportInfo?,
        positionInfo: PositionInfo?,
        onResult: (TransportInfo, PositionInfo) -> Unit,
    ) {
        if (transportInfo == null || positionInfo == null) {
            log.e("Exiting combine result! TransportInfo is null: ${transportInfo == null}, PositionInfo is null: ${positionInfo == null}")
            return
        }

        onResult(transportInfo, positionInfo)
    }

    override fun selectContentDirectoryAsync(upnpDevice: UpnpDevice): Deferred<Result> = async {
        folderStack.value = listOf()
        contentCache.clear()
        contentDirectoryObservable.selectedContentDirectory = upnpDevice

        safeNavigateTo(
            folderId = ROOT_FOLDER_ID,
            folderName = upnpDevice.friendlyName
        )
    }

    override fun selectRenderer(spinnerItem: SpinnerItem) {
        launch {
            val renderer: UpnpDevice = spinnerItem.deviceDisplay.upnpDevice

            isLocal = renderer.isLocal

            if (isLocal || renderer != rendererDiscoveryObservable.getSelectedRenderer())
                stopUpdate()

            if (!isLocal) {
                rendererDiscoveryObservable.selectRenderer(renderer)
            } else {
                rendererDiscoveryObservable.selectRenderer(null)
            }
        }
    }

    private fun renderItem(item: RenderItem): Flow<Result> {
        stopUpdate()

        if (isLocal) {
            launchLocally(item)
            return flowOf(Result.Success)
        }

        return flow<Result> {
            getAvService()
                .flatMapLatest { service ->
                    val didlItem = item.didlItem.didlObject as Item
                    val uri = didlItem.firstResource?.value ?: error("First resource or its value is null!")
                    val didlType = didlType(didlItem)

                    upnpRepository
                        .setUriFlow(service, uri, newMetadata(didlItem, didlType))
                        .flatMapLatest { upnpRepository.playFlow(service) }
                        .onEach {
                            when (didlItem) {
                                is AudioItem,
                                is VideoItem,
                                -> updateChannel.emit(didlItem to service)
                                is ImageItem -> upnpInnerStateChannel.emit(UpnpRendererState.Empty)
                            }
                        }
                }
                .onCompletion { cause ->
                    if (cause == null)
                        emit(Result.Success)
                    else
                        emit(Result.Error)
                }.collect()
        }.catch { e ->
            log.e(e)
            emit(Result.Error)
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
        launch {
            stopPlayback()
            updateChannel.emit(null)
        }
    }

    private var currentIndex: Int = -1

    override fun playNext() {
        launch {
            if (currentIndex in 0 until currentContent.value.size - 1)
                renderItem(RenderItem(currentContent.value[++currentIndex])).collect()
        }
    }

    override fun itemClick(id: String): Flow<Result> {
        val item: ClingDIDLObject = contentCache[id] ?: return flowOf(Result.Error)

        return when (item) {
            is ClingContainer -> navigateTo(item.id, item.title)
            is ClingMedia -> playItem(item)
            is MiscItem -> flowOf(Result.Error)
        }
    }

    private fun playItem(item: ClingDIDLObject): Flow<Result> = flow {
        currentIndex = currentContent.value.indexOf(item)
        emitAll(renderItem(RenderItem(item)))
    }.flowOn(Dispatchers.IO)

    override fun playPrevious() {
        launch {
            if (currentIndex in 1 until currentContent.value.size)
                renderItem(RenderItem(currentContent.value[--currentIndex])).collect()
        }
    }

    override fun pausePlayback() {
        launch {
            getAvService()
                .flatMapLatest { service -> upnpRepository.pauseFlow(service) }
                .catch("Failed to pause playback")
                .collect()
        }
    }

    override fun stopPlayback() {
        launch {
            getAvService()
                .flatMapLatest { service -> upnpRepository.stopFlow(service) }
                .catch("Failed to stop playback").collect()
        }
    }

    override fun resumePlayback() {
        launch {
            getAvService()
                .flatMapLatest { service -> upnpRepository.playFlow(service) }
                .catch("Failed to resume playback")
                .collect()
        }
    }

    override fun seekTo(progress: Int) {
        launch {
            getAvService()
                .onStart { pauseUpdate = true }
                .flatMapLatest { service ->
                    upnpRepository.seekToFlow(
                        service = service,
                        time = formatTime(
                            max = MAX_PROGRESS,
                            progress = progress,
                            duration = currentDuration
                        )
                    )
                }
                .onEach { pauseUpdate = false }
                .catch("Failed to seek to progress!")
                .collect()
        }
    }


    override suspend fun raiseVolume(step: Int) {
        getRcService()
            .catch("Failed to raise volume with step $step!")
            .collect { service -> volumeRepository.raiseVolume(service, step) }
    }

    override suspend fun lowerVolume(step: Int) {
        getRcService()
            .catch("Failed to lower volume with step $step!")
            .collect { service -> volumeRepository.lowerVolume(service, step) }
    }

    override suspend fun muteVolume(mute: Boolean) {
        getRcService()
            .catch("Failed to set mute to $mute!")
            .collect { service -> volumeRepository.muteVolume(service, mute) }
    }

    override suspend fun setVolume(volume: Int) {
        getRcService()
            .catch("Failed to set volume to $volume!")
            .collect { service -> volumeRepository.setVolume(service, volume) }
    }

    override suspend fun getVolume(): Flow<Int> = getRcService().map { service -> volumeRepository.getVolume(service) }

    override fun navigateTo(folder: Folder) {
        val index = folderStack.value.indexOf(folder)

        if (index == -1) {
            log.e("Folder $folder isn't found in navigation stack!")
            return
        }

        folderStack.value = folderStack.value.subList(0, index + 1)
    }

    private fun navigateTo(id: String, folderName: String): Flow<Result> = flow {
        emit(
            safeNavigateTo(
                folderId = id,
                folderName = folderName
            )
        )
    }.flowOn(Dispatchers.IO)

    override fun navigateBack() {
        folderStack.value = folderStack.value.dropLast(1)
    }

    override fun togglePlayback() {
        launch {
            getAvService()
                .flatMapLatest { service ->
                    if (remotePaused)
                        upnpRepository.playFlow(service)
                    else
                        upnpRepository.pauseFlow(service)
                }
                .catch("Failed to toggle playback ($remotePaused)!")
                .collect()
        }
    }

    private val contentCache: MutableMap<String, ClingDIDLObject> = mutableMapOf()

    private var currentContent: MutableStateFlow<List<ClingDIDLObject>> = MutableStateFlow(listOf())

    private val folderStack: MutableStateFlow<List<Folder>> = MutableStateFlow(listOf())

    override val navigationStack: Flow<List<Folder>> = folderStack.onEach { folders ->
        if (folders.isEmpty()) {
            stopUpdate()
            rendererDiscoveryObservable.selectRenderer(null)
        }
    }

    private suspend inline fun safeNavigateTo(
        folderId: String,
        folderName: String,
    ): Result {
        Timber.d("Navigating to $folderId with name $folderName")

        val selectedDevice = contentDirectoryObservable.selectedContentDirectory

        return if (selectedDevice == null) {
            log.e("Selected content directory is null!")
            Result.Error
        } else {
            val service: Service<*, *>? =
                (selectedDevice as CDevice).device.findService(UDAServiceType(CONTENT_DIRECTORY))

            if (service == null || !service.hasActions()) {
                return Result.Error
            }

            currentContent.value = upnpRepository.browse(service, folderId)
            contentCache.putAll(currentContent.value.associateBy { it.id })
            val currentFolderName = folderName.replace(UpnpContentRepositoryImpl.USER_DEFINED_PREFIX, "")

            val folder = when (folderId) {
                ROOT_FOLDER_ID -> Folder.Root(
                    FolderModel(
                        id = folderId,
                        title = currentFolderName,
                        contents = currentContent.value
                    )
                )
                else -> Folder.SubFolder(
                    FolderModel(
                        id = folderId,
                        title = currentFolderName,
                        contents = currentContent.value
                    )
                )
            }

            folderStack.value = when (folder) {
                is Folder.Root -> listOf(folder)
                is Folder.SubFolder -> folderStack
                    .value
                    .toMutableList()
                    .apply { add(folder) }
                    .toList()
            }

            Result.Success
        }
    }

    private fun getAvService(): Flow<Service<*, *>> = flow {
        rendererDiscoveryObservable.getSelectedRenderer()?.let { renderer ->
            val service: Service<*, *> = (renderer as CDevice).device.findService(UDAServiceType(AV_TRANSPORT))
                ?: error("AvService is not found!")

            if (service.hasActions()) {
                emit(service)
            } else {
                error("AvService doesn't have actions!")
            }
        } ?: error("getAvTransport: Selected renderer is null!")
    }

    private fun getRcService(): Flow<Service<*, *>> = flow {
        rendererDiscoveryObservable.getSelectedRenderer()?.let { renderer ->
            val service: Service<*, *> = (renderer as CDevice).device.findService(UDAServiceType(RENDERING_CONTROL))
                ?: error("RcService is not found!")

            if (service.hasActions())
                emit(service)
            else {
                error("RcService doesn't have actions!")
            }
        } ?: error("getRcService: Selected renderer is null!")
    }

    private fun <T> Flow<T>.catch(message: String): Flow<T> = catch { e -> log.e(e, message) }
}

inline class RenderItem(val didlItem: ClingDIDLObject)

