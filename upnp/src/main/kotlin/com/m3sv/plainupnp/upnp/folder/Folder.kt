package com.m3sv.plainupnp.upnp.folder

import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject

sealed class Folder(val folderModel: FolderModel) {
    data class Root(private val model: FolderModel) : Folder(model)
    data class SubFolder(private val model: FolderModel) : Folder(model)
}

data class FolderModel(
    val id: String,
    val title: String,
    val contents: List<ClingDIDLObject>
)
