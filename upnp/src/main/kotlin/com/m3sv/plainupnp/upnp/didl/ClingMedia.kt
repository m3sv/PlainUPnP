package com.m3sv.plainupnp.upnp.didl

import org.fourthline.cling.support.model.DIDLObject
import org.fourthline.cling.support.model.item.AudioItem
import org.fourthline.cling.support.model.item.ImageItem
import org.fourthline.cling.support.model.item.VideoItem

sealed class ClingMedia(item: DIDLObject) : ClingDIDLObject(item) {
    class Video(item: VideoItem) : ClingMedia(item)
    class Image(item: ImageItem) : ClingMedia(item)
    class Audio(item: AudioItem) : ClingMedia(item)
}
