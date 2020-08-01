package com.m3sv.plainupnp.upnp.manager

import com.m3sv.plainupnp.common.Consumable
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.folder.FolderType
import com.m3sv.plainupnp.upnp.playback.PlaybackManager
import com.m3sv.plainupnp.upnp.volume.UpnpVolumeManager
import kotlinx.coroutines.flow.Flow

interface UpnpManager : UpnpVolumeManager, PlaybackManager {
    val renderers: Flow<List<DeviceDisplay>>
    val contentDirectories: Flow<List<DeviceDisplay>>
    val upnpRendererState: Flow<UpnpRendererState>
    val actionErrors: Flow<Consumable<String>>
    val folderStructureFlow: Flow<List<FolderType>>
    val folderChangeFlow: Flow<Consumable<FolderType>>

    fun selectContentDirectory(position: Int)
    fun selectRenderer(position: Int)

    // TODO Split Object into Media and Folder
    fun playItem(
        clingDIDLObject: ClingDIDLObject,
        listIterator: ListIterator<ClingDIDLObject>
    )

    fun navigateTo(folderId: String, title: String)
    fun navigateBack()
    fun seekTo(progress: Int)
    fun getCurrentFolderContents(): List<ClingDIDLObject>
    fun getCurrentFolderName(): String
}
