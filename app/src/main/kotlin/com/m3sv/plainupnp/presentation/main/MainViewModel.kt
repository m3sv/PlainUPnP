package com.m3sv.plainupnp.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val upnpManager: UpnpManager,
    private val volumeManager: BufferedVolumeManager,
    private val filterDelegate: FilterDelegate
) : BaseViewModel<MainIntention, MainState>(MainState.Initial) {


    val volume = volumeManager.observeVolume()

    init {
        observeUpnpManager()
        observeUpnpState()
    }

    private val upnpState: MutableLiveData<UpnpRendererState> = MutableLiveData()

    fun upnpState(): LiveData<UpnpRendererState> = upnpState

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

    private fun observeUpnpState() {
        viewModelScope.launch {
            upnpManager.upnpRendererState.collect { state ->
                upnpState.postValue(state)
            }
        }
    }

    private fun observeUpnpManager() {
        with(upnpManager) {
            Observable
                .combineLatest<List<SpinnerItem>, List<SpinnerItem>, MainState>(
                    observeRenderers(),
                    observeContentDirectories(),
                    BiFunction(MainState::Render)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { newState -> viewModelScope.launch { updateState { newState } } }
                .disposeBy(disposables)
        }
    }

    private fun UpnpManager.observeContentDirectories() =
        contentDirectories.map { directories -> directories.map { SpinnerItem(it.device.friendlyName) } }

    private fun UpnpManager.observeRenderers() =
        renderers.map { renderers -> renderers.map { SpinnerItem(it.device.friendlyName) } }

    private fun filterText(text: String) {
        viewModelScope.launch { filterDelegate.filter(text) }
    }
}
