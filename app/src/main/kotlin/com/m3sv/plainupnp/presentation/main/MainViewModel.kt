package com.m3sv.plainupnp.presentation.main

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val upnpManager: UpnpManager,
    private val volumeManager: BufferedVolumeManager,
    private val filterDelegate: FilterDelegate
) : BaseViewModel<MainIntention>() {

    val volume = volumeManager
        .observeVolume()
        .asLiveData()

    val upnpState = upnpManager
        .upnpRendererState
        .asLiveData()

    val renderers =
        upnpManager
            .renderers
            .map { renderers -> renderers.map { SpinnerItem(it.device.friendlyName) } }
            .asLiveData()

    val contentDirectories =
        upnpManager
            .contentDirectories
            .map { directories -> directories.map { SpinnerItem(it.device.friendlyName) } }
            .asLiveData()

    override fun intention(intention: MainIntention) {
        viewModelScope.launch {
            when (intention) {
                is MainIntention.ResumeUpnp -> upnpManager.resumeRendererUpdate()
                is MainIntention.PauseUpnp -> upnpManager.pauseRendererUpdate()
                is MainIntention.MoveTo -> upnpManager.moveTo(intention.progress)
                is MainIntention.SelectContentDirectory ->
                    upnpManager.selectContentDirectory(intention.position)
                is MainIntention.SelectRenderer -> upnpManager.selectRenderer(intention.position)
                is MainIntention.PlayerButtonClick -> handleButtonClick(intention.button)
                is MainIntention.Filter -> filterText(intention.text)
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
