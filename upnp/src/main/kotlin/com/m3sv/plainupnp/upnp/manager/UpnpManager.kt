package com.m3sv.plainupnp.upnp.manager

import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.presentation.SpinnerItem
import com.m3sv.plainupnp.upnp.folder.Folder
import com.m3sv.plainupnp.upnp.playback.PlaybackManager
import com.m3sv.plainupnp.upnp.volume.UpnpVolumeManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

interface UpnpManager : UpnpVolumeManager, PlaybackManager {
    val isConnectedToRenderer: Flow<Boolean>
    val renderers: Flow<List<DeviceDisplay>>
    val contentDirectories: Flow<List<DeviceDisplay>>
    val upnpRendererState: Flow<UpnpRendererState>
    val navigationStack: Flow<List<Folder>>

    fun navigateBack()
    fun navigateTo(folder: Folder)
    fun itemClick(id: String): Flow<Result>
    fun seekTo(progress: Int)
    fun selectContentDirectoryAsync(upnpDevice: UpnpDevice): Deferred<Result>
    fun selectRenderer(spinnerItem: SpinnerItem)
}
