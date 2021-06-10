package com.m3sv.plainupnp.upnp.didl

import org.fourthline.cling.support.model.DIDLObject
import org.fourthline.cling.support.model.item.AudioItem
import org.fourthline.cling.support.model.item.ImageItem
import org.fourthline.cling.support.model.item.VideoItem

sealed class ClingMedia(item: DIDLObject) : ClingDIDLObject(item) {
    data class Video(private val item: VideoItem) : ClingMedia(item)
    data class Image(private val item: ImageItem) : ClingMedia(item)
    data class Audio(private val item: AudioItem) : ClingMedia(item)
}
