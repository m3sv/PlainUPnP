package com.m3sv.plainupnp.upnp.manager

import com.m3sv.plainupnp.common.Consumable
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.folder.Folder
import com.m3sv.plainupnp.upnp.playback.PlaybackManager
import com.m3sv.plainupnp.upnp.volume.UpnpVolumeManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

interface UpnpManager : UpnpVolumeManager, PlaybackManager {
    val isConnectedToRender: Flow<UpnpDevice?>
    val isContentDirectorySelected: Boolean
    val renderers: Flow<List<DeviceDisplay>>
    val contentDirectories: Flow<List<DeviceDisplay>>
    val upnpRendererState: Flow<UpnpRendererState>
    val actionErrors: Flow<Consumable<String>>
    val folderChangeFlow: Flow<Folder>

    fun selectContentDirectory(position: Int)
    fun selectContentDirectoryAsync(upnpDevice: UpnpDevice): Deferred<Result>

    fun selectRenderer(position: Int)

    // TODO Split Object into Media and Folder
    fun playItem(playItem: PlayItem)
    fun openFolder(folder: Folder)
    fun seekTo(progress: Int)
    fun getCurrentFolderContents(): List<ClingDIDLObject>
    fun getCurrentFolderName(): String
}
