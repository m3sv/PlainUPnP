package com.m3sv.plainupnp.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.m3sv.plainupnp.common.Consumable
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.core.eventbus.subscribe
import com.m3sv.plainupnp.presentation.home.FolderClick
import com.m3sv.plainupnp.presentation.home.MediaItemClick
import com.m3sv.plainupnp.presentation.home.MediaItemLongClick
import com.m3sv.plainupnp.upnp.didl.ClingMedia
import com.m3sv.plainupnp.upnp.discovery.device.ObserveContentDirectoriesUseCase
import com.m3sv.plainupnp.upnp.discovery.device.ObserveRenderersUseCase
import com.m3sv.plainupnp.upnp.folder.Folder
import com.m3sv.plainupnp.upnp.manager.PlayItem
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class MainRoute {
    object Initial : MainRoute()
    object Settings : MainRoute()
    data class Back(val folder: Folder?) : MainRoute()
    data class ToFolder(val folder: Folder) : MainRoute()
    data class PreviewImage(val url: String) : MainRoute()
    data class PreviewVideo(val url: String) : MainRoute()
    data class PreviewAudio(val url: String) : MainRoute()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val upnpManager: UpnpManager,
    private val volumeManager: BufferedVolumeManager,
    private val filterDelegate: FilterDelegate,
    private val deviceDisplayMapper: DeviceDisplayMapper,
    private val folderManager: FolderManager,
    observeContentDirectories: ObserveContentDirectoriesUseCase,
    observeRenderersUseCase: ObserveRenderersUseCase,
) : ViewModel() {

    init {
        viewModelScope.launch {
            subscribe<MediaItemClick>()
                .map { it.data as PlayItem }
                .collect { item -> upnpManager.playItem(item) }
        }

        viewModelScope.launch {
            subscribe<FolderClick>()
                .map { it.data as Folder }
                .collect { folder -> navigate(MainRoute.ToFolder(folder)) }
        }

        viewModelScope.launch {
            subscribe<MediaItemLongClick>()
                .map { it.data as PlayItem }
                .collect { item ->
                    val route: MainRoute = when (item.clingDIDLObject) {
                        is ClingMedia.Image -> MainRoute.PreviewImage(requireNotNull(item.clingDIDLObject.uri))
                        is ClingMedia.Video -> MainRoute.PreviewVideo(requireNotNull(item.clingDIDLObject.uri))
                        is ClingMedia.Audio -> MainRoute.PreviewAudio(requireNotNull(item.clingDIDLObject.uri))
                        else -> error("Unknown media type")
                    }

                    navigate(route)
                }
        }
    }

    val volume = volumeManager
        .volumeFlow
        .asLiveData()

    val upnpState = upnpManager
        .upnpRendererState
        .asLiveData()

    val renderers = observeRenderersUseCase()
        .map { bundle -> deviceDisplayMapper(bundle) }
        .asLiveData()

    val contentDirectories = observeContentDirectories()
        .map { bundle -> deviceDisplayMapper(bundle) }
        .asLiveData()

    val errors = upnpManager
        .actionErrors
        .asLiveData()

    val changeFolder = upnpManager
        .folderChangeFlow
        .map { Consumable(it) }
        .asLiveData()

    val navigationStrip = folderManager
        .observe()
        .asLiveData()

    private val navigationChannel: BroadcastChannel<MainRoute> = BroadcastChannel(1)

    val navigation: LiveData<Consumable<MainRoute>> = navigationChannel
        .asFlow()
        .scan<MainRoute, MainRoute>(MainRoute.Initial) { previous, next ->
            when (previous) {
                is MainRoute.Initial -> {
                    when (next) {
                        is MainRoute.ToFolder -> next.apply { upnpManager.openFolder(folder) }
                        else -> next
                    }
                }
                is MainRoute.Settings -> next
                is MainRoute.Back -> when (next) {
                    is MainRoute.ToFolder -> next.apply { upnpManager.openFolder(folder) }
                    is MainRoute.Back -> next.apply { folderManager.backTo(folder) }
                    else -> next
                }
                is MainRoute.ToFolder -> when (next) {
                    is MainRoute.ToFolder -> next.apply { upnpManager.openFolder(folder) }
                    is MainRoute.Back -> next.apply { folderManager.backTo(folder) }
                    else -> next
                }
                is MainRoute.PreviewImage,
                is MainRoute.PreviewAudio,
                is MainRoute.PreviewVideo,
                -> next
            }

        }
        .map { Consumable(it) }
        .asLiveData()

    fun moveTo(progress: Int) {
        viewModelScope.launch {
            upnpManager.seekTo(progress)
        }
    }

    fun selectContentDirectory(position: Int) {
        upnpManager.selectContentDirectory(position)
    }

    fun selectRenderer(position: Int) {
        upnpManager.selectRenderer(position)
    }

    fun playerButtonClick(button: PlayerButton) {
        viewModelScope.launch {
            when (button) {
                PlayerButton.PLAY -> upnpManager.togglePlayback()
                PlayerButton.PREVIOUS -> upnpManager.playPrevious()
                PlayerButton.NEXT -> upnpManager.playNext()
                PlayerButton.RAISE_VOLUME -> volumeManager.raiseVolume()
                PlayerButton.LOWER_VOLUME -> volumeManager.lowerVolume()
            }
        }
    }

    fun filterText(text: String) {
        viewModelScope.launch { filterDelegate.filter(text) }
    }

    fun navigate(route: MainRoute) {
        navigationChannel.offer(route)
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("OnCleared is called")
    }
}
