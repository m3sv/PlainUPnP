package com.m3sv.plainupnp.presentation.main.data

import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import com.m3sv.plainupnp.upnp.didl.ClingAudioItem
import com.m3sv.plainupnp.upnp.didl.ClingDIDLContainer
import com.m3sv.plainupnp.upnp.didl.ClingImageItem
import com.m3sv.plainupnp.upnp.didl.ClingVideoItem

fun List<DIDLObjectDisplay>?.toItems():List<Item> = this?.map {
    when (it.didlObject) {
        is ClingDIDLContainer -> {
            Item(
                it.didlObject.id,
                it.title,
                ContentType.DIRECTORY,
                this,
                it.didlObject.parentID
            )
        }

        is ClingImageItem -> {
            Item(
                (it.didlObject as ClingImageItem).uri,
                it.title,
                ContentType.IMAGE,
                this
            )
        }

        is ClingVideoItem -> {
            Item(
                (it.didlObject as ClingVideoItem).uri,
                it.title,
                ContentType.VIDEO,
                this
            )
        }

        is ClingAudioItem -> {
            Item(
                (it.didlObject as ClingAudioItem).uri,
                it.title,
                ContentType.AUDIO,
                this
            )
        }

        else -> throw IllegalStateException("Unknown DIDLObject")
    }
} ?: listOf()

data class Item(
    val uri: String?,
    val name: String,
    val type: ContentType,
    val didlObjectDisplay: List<DIDLObjectDisplay>? = null,
    val parentId: String? = null
)

enum class ContentType {
    IMAGE, AUDIO, VIDEO, DIRECTORY
}