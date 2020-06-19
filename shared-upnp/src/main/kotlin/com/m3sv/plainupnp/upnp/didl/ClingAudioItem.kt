package com.m3sv.plainupnp.upnp.didl

import org.fourthline.cling.support.model.item.AudioItem

class ClingAudioItem(item: AudioItem) : ClingDIDLItem(item) {

    override val dataType: String = "audio/*"

}
