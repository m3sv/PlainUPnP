package com.m3sv.selectcontentdirectory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m3sv.plainupnp.ThemeOption
import com.m3sv.plainupnp.common.preferences.PreferencesRepository
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.manager.Result
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SelectContentDirectoryViewState(
    val activeTheme: ThemeOption,
    val contentDirectories: List<DeviceDisplay>
)

@HiltViewModel
class SelectContentDirectoryViewModel @Inject constructor(
    preferences: PreferencesRepository,
    private val upnpManager: UpnpManager,
) : ViewModel() {

    val state: StateFlow<SelectContentDirectoryViewState> =
        combine(
            upnpManager.contentDirectories,
            preferences.theme
        ) { directories, theme ->
            SelectContentDirectoryViewState(activeTheme = theme, contentDirectories = directories)
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            SelectContentDirectoryViewState(preferences.theme.value, listOf())
        )

    fun selectContentDirectoryAsync(upnpDevice: UpnpDevice): Deferred<Result> =
        upnpManager.selectContentDirectoryAsync(upnpDevice)
}
