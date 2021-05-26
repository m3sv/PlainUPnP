package com.m3sv.plainupnp.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import com.m3sv.plainupnp.upnp.didl.ClingContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.didl.ClingMedia
import com.m3sv.plainupnp.upnp.discovery.device.ObserveRenderersUseCase
import com.m3sv.plainupnp.upnp.folder.Folder
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val upnpManager: UpnpManager,
    private val volumeManager: BufferedVolumeManager,
    private val filterDelegate: FilterDelegate,
    private val deviceDisplayMapper: DeviceDisplayMapper,
    observeRenderersUseCase: ObserveRenderersUseCase,
) : ViewModel() {

    val volume = volumeManager
        .volumeFlow

    val upnpState = upnpManager
        .upnpRendererState

    val renderers = observeRenderersUseCase()
        .map { bundle -> deviceDisplayMapper(bundle) }

    val navigationStack: Flow<List<Folder>> = upnpManager.navigationStack

    fun itemClick(item: ClingDIDLObject) {
        when (item) {
            is ClingContainer -> navigateTo(item.id, item.title)
            is ClingMedia -> upnpManager.playItem(item)
            else -> error("Unknown cling item")
        }

    }

    fun moveTo(progress: Int) {
        viewModelScope.launch {
            upnpManager.seekTo(progress)
        }
    }

    fun selectRenderer(position: SpinnerItem) {
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
                PlayerButton.STOP -> upnpManager.stopPlayback()
            }
        }
    }

    fun filterText(text: String) {
        viewModelScope.launch { filterDelegate.filter(text) }
    }

    fun navigateBack() {
        upnpManager.navigateBack()
    }

    fun navigateTo(folder: Folder) {
        upnpManager.navigateTo(folder)
    }

    private fun navigateTo(id: String, title: String) {
        upnpManager.navigateTo(id, title)
    }
}
