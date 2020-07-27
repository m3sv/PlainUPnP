package com.m3sv.plainupnp.presentation.main

import androidx.lifecycle.*
import com.m3sv.plainupnp.ShutdownNotifier
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.upnp.discovery.device.ObserveContentDirectoriesUseCase
import com.m3sv.plainupnp.upnp.discovery.device.ObserveRenderersUseCase
import com.m3sv.plainupnp.upnp.folder.FolderType
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val upnpManager: UpnpManager,
    private val volumeManager: BufferedVolumeManager,
    private val filterDelegate: FilterDelegate,
    // TODO research why Dagger doesn't like Kotlin generic, use concrete implementation for now
    private val deviceDisplayMapper: DeviceDisplayMapper,
    observeContentDirectories: ObserveContentDirectoriesUseCase,
    observeRenderersUseCase: ObserveRenderersUseCase,
    shutdownNotifier: ShutdownNotifier
) : ViewModel() {

    val shutdown: LiveData<Unit> = shutdownNotifier.flow.asLiveData()

    val volume = volumeManager
        .volumeFlow
        .asLiveData()

    val upnpState = upnpManager
        .upnpRendererState
        .asLiveData()

    val renderers = observeRenderersUseCase()
        .map { bundle -> deviceDisplayMapper.map(bundle) }
        .asLiveData()

    val contentDirectories = observeContentDirectories()
        .map { bundle -> deviceDisplayMapper.map(bundle) }
        .asLiveData()

    val errors = upnpManager
        .actionErrors
        .asLiveData()

    private val folderStructure: Deque<String> = LinkedList<String>()

    private val mutableNavigationState = MutableLiveData<List<String>>()

    val navigationState: LiveData<List<String>> = mutableNavigationState

    val changeFolder = upnpManager
        .folderChangeFlow
        .map { consumable ->
            when (val value = consumable.peek()) {
                is FolderType.Root -> with(folderStructure) {
                    clear()
                    add(value.name)
                }

                is FolderType.SubFolder -> folderStructure.add(value.name)
            }

            updateNavigationUi()

            consumable
        }
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

    fun navigateBack() {
        folderStructure.pollLast()
        updateNavigationUi()
    }

    private fun updateNavigationUi() {
        mutableNavigationState.postValue(folderStructure.toList())
    }
}
