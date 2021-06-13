package com.m3sv.plainupnp.upnp.didl

import org.fourthline.cling.support.model.DIDLObject
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.AudioItem
import org.fourthline.cling.support.model.item.ImageItem
import org.fourthline.cling.support.model.item.VideoItem

sealed class ClingDIDLObject(
    val didlObject: DIDLObject,
    val id: String = didlObject.id,
    val title: String = didlObject.title,
    val uri: String? = didlObject.firstResource?.value
) {
    override fun toString(): String = "[${this.javaClass}, [$id, $title, $uri]]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClingDIDLObject

        if (uri != other.uri) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uri?.hashCode() ?: 0
        result = 31 * result + id.hashCode()
        return result
    }
}

sealed class ClingMedia(item: DIDLObject) : ClingDIDLObject(item) {
    data class Video(private val item: VideoItem) : ClingMedia(item)
    data class Image(private val item: ImageItem) : ClingMedia(item)
    data class Audio(private val item: AudioItem) : ClingMedia(item)
}

data class ClingContainer(private val item: Container) : ClingDIDLObject(item) {
    override fun toString(): String = super.toString()
}

data class MiscItem(private val item: DIDLObject) : ClingDIDLObject(item)


