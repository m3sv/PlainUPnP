package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.upnp.didl.ClingContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.didl.ClingMedia
import com.m3sv.plainupnp.upnp.didl.MiscItem

enum class ItemType {
    CONTAINER,
    AUDIO,
    VIDEO,
    IMAGE,
    MISC
}

internal fun ClingDIDLObject.toItemType(): ItemType = when (this) {
    is ClingContainer -> ItemType.CONTAINER
    is ClingMedia.Audio -> ItemType.AUDIO
    is ClingMedia.Image -> ItemType.IMAGE
    is ClingMedia.Video -> ItemType.VIDEO
    is MiscItem -> ItemType.MISC
}

data class ItemViewModel(
    val id: String,
    val title: String,
    val type: ItemType,
    val uri: String?,
)

sealed class FolderContents {
    object Empty : FolderContents()
    data class Contents(val items: List<ItemViewModel>) : FolderContents()
}
