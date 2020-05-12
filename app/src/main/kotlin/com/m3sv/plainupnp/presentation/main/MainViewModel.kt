package com.m3sv.plainupnp.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.m3sv.plainupnp.ShutdownNotifier
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val upnpManager: UpnpManager,
    private val volumeManager: BufferedVolumeManager,
    private val filterDelegate: FilterDelegate,
    // TODO research why Dagger doesn't like Kotlin generic, use concrete implementation for now
    private val deviceDisplayMapper: DeviceDisplayMapper,
    shutdownNotifier: ShutdownNotifier
) : BaseViewModel<MainIntention>() {

    val shutdown: LiveData<Unit> = shutdownNotifier.flow.asLiveData()

    val volume = volumeManager
        .observeVolume()
        .asLiveData()

    val upnpState = upnpManager
        .upnpRendererState
        .asLiveData()

    val renderers = upnpManager
        .renderers
        .map { renderers -> deviceDisplayMapper.map(renderers) }
        .asLiveData()

    val contentDirectories = upnpManager
        .contentDirectories
        .map { directories -> deviceDisplayMapper.map(directories) }
        .asLiveData()

    override fun intention(intention: MainIntention) {
        viewModelScope.launch {
            with(upnpManager) {
                when (intention) {
                    is MainIntention.ResumeUpnp -> resumeRendererUpdate()
                    is MainIntention.PauseUpnp -> pauseRendererUpdate()
                    is MainIntention.MoveTo -> moveTo(intention.progress)
                    is MainIntention.SelectContentDirectory -> selectContentDirectory(intention.position)
                    is MainIntention.SelectRenderer -> selectRenderer(intention.position)
                    is MainIntention.PlayerButtonClick -> handleButtonClick(intention.button)
                    is MainIntention.Filter -> filterText(intention.text)
                }
            }
        }
    }

    private suspend fun handleButtonClick(button: PlayerButton) {
        when (button) {
            PlayerButton.PLAY -> upnpManager.togglePlayback()
            PlayerButton.PREVIOUS -> upnpManager.playPrevious()
            PlayerButton.NEXT -> upnpManager.playNext()
            PlayerButton.RAISE_VOLUME -> volumeManager.raiseVolume()
            PlayerButton.LOWER_VOLUME -> volumeManager.lowerVolume()
        }
    }

    private fun filterText(text: String) {
        viewModelScope.launch { filterDelegate.filter(text) }
    }
}
