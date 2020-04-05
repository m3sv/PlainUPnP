package com.m3sv.plainupnp.upnp.didl

import com.m3sv.plainupnp.upnp.R
import org.fourthline.cling.support.model.item.AudioItem

class ClingAudioItem(item: AudioItem) : ClingDIDLItem(item) {

    override val dataType: String = "audio/*"

    override val icon: Int = R.drawable.ic_action_headphones

}
