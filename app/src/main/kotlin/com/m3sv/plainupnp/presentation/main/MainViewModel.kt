package com.m3sv.plainupnp.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m3sv.plainupnp.common.preferences.PreferencesRepository
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.presentation.SpinnerItem
import com.m3sv.plainupnp.upnp.folder.Folder
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainViewState(
    val upnpRendererState: UpnpRendererState = UpnpRendererState.Empty,
    val renderers: SpinnerItemsBundle = SpinnerItemsBundle.empty,
    val enableThumbnails: Boolean = false,
)

sealed class VolumeUpdate(val volume: Int) {
    class Show(volume: Int) : VolumeUpdate(volume)
    class Hide(volume: Int) : VolumeUpdate(volume)
}

@HiltViewModel
class MainViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    private val upnpManager: UpnpManager,
    private val volumeManager: BufferedVolumeManager,
) : ViewModel() {

    val isConnectedToRenderer: Flow<Boolean> = upnpManager.isConnectedToRenderer

    val volume: StateFlow<VolumeUpdate> = volumeManager
        .volumeFlow
        .flatMapLatest { volume ->
            flow {
                emit(VolumeUpdate.Show(volume))
                delay(2500)
                emit(VolumeUpdate.Hide(volume))
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = VolumeUpdate.Hide(-1)
        )

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _filterText: MutableStateFlow<String> = MutableStateFlow("")

    val filterText: StateFlow<String> = _filterText

    val navigation: StateFlow<List<Folder>> = upnpManager
        .navigationStack
        .filterNot { it.isEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = listOf()
        )

    private val upnpState = upnpManager
        .upnpRendererState
        .onStart { emit(UpnpRendererState.Empty) }

    private val renderers = upnpManager
        .renderers
        .map { devices ->
            val items = devices.map { SpinnerItem(it.upnpDevice.friendlyName, it) }
            SpinnerItemsBundle(items)
        }

    val finishActivityFlow: Flow<Unit> = upnpManager
        .navigationStack
        .filter { it.isEmpty() }
        .map { }

    val viewState: StateFlow<MainViewState> =
        combine(
            upnpState,
            renderers,
            preferencesRepository.preferences.map { it.enableThumbnails }
        ) { upnpRendererState, spinnerItemsBundle, enableThumbnails ->
            MainViewState(
                upnpRendererState = upnpRendererState,
                renderers = spinnerItemsBundle,
                enableThumbnails = enableThumbnails
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = MainViewState()
        )

    fun itemClick(id: String) {
        viewModelScope.launch {
            upnpManager
                .itemClick(id)
                .onStart { _loading.value = true }
                .onCompletion { _loading.value = false }
                .collect()
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

    fun navigateBack() {
        upnpManager.navigateBack()
    }

    fun navigateTo(folder: Folder) {
        upnpManager.navigateTo(folder)
    }

    fun filterInput(text: String) {
        viewModelScope.launch {
            _filterText.emit(text)
        }
    }
}
