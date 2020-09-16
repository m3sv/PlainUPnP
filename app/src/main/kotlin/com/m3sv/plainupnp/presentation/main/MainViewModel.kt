package com.m3sv.plainupnp.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.m3sv.plainupnp.common.Consumable
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.upnp.PlainUpnpAndroidService
import com.m3sv.plainupnp.upnp.discovery.device.ObserveContentDirectoriesUseCase
import com.m3sv.plainupnp.upnp.discovery.device.ObserveRenderersUseCase
import com.m3sv.plainupnp.upnp.folder.Folder
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MainRoute {
    object Settings : MainRoute()
    data class BackTo(val folder: Folder?) : MainRoute()
    data class ToFolder(val folder: Folder) : MainRoute()
}

class MainViewModel @Inject constructor(
    private val upnpManager: UpnpManager,
    private val volumeManager: BufferedVolumeManager,
    private val filterDelegate: FilterDelegate,
    private val deviceDisplayMapper: DeviceDisplayMapper,
    private val folderManager: FolderManager,
    observeContentDirectories: ObserveContentDirectoriesUseCase,
    observeRenderersUseCase: ObserveRenderersUseCase,
) : ViewModel() {

    val finishFlow = PlainUpnpAndroidService
        .finishFlow
        .asLiveData()

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
        .scan<MainRoute, Consumable<MainRoute>>(Consumable()) { current, next ->
            when (next) {
                is MainRoute.Settings -> Unit
                is MainRoute.BackTo -> handleNavigateBack(current.peek(), next)
                is MainRoute.ToFolder -> upnpManager.openFolder(next.folder)
            }

            Consumable(next)
        }
        .asLiveData()

    private fun handleNavigateBack(previous: MainRoute?, next: MainRoute) {
        when (previous) {
            is MainRoute.Settings -> Unit
            else -> {
                if (next is MainRoute.BackTo) {
                    folderManager.backTo(next.folder)
                }
            }
        }
    }

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
}
