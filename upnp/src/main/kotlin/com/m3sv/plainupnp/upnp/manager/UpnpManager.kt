package com.m3sv.plainupnp.upnp.manager

import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.presentation.SpinnerItem
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
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
    val folderChangeFlow: Flow<Folder>
    val navigationStack: Flow<List<Folder>>

    fun getCurrentFolderContents(): List<ClingDIDLObject>
    fun getCurrentFolderName(): String
    fun navigateBack()
    fun navigateTo(folder: Folder)
    fun navigateTo(id: String, folderName: String)
    fun playItem(id: String)
    fun seekTo(progress: Int)
    fun selectContentDirectoryAsync(upnpDevice: UpnpDevice): Deferred<Result>
    fun selectRenderer(spinnerItem: SpinnerItem)
}
