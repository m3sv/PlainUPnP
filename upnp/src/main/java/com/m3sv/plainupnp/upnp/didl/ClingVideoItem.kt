package com.m3sv.plainupnp.upnp.didl

import com.m3sv.upnp.R
import org.fourthline.cling.support.model.item.VideoItem

class ClingVideoItem(item: VideoItem) : ClingDIDLItem(item) {
    override val dataType: String = "video/*"

    override val description: String = didlObject.resources
            ?.takeIf { it.isNotEmpty() }
            ?.get(0)
            ?.resolution ?: ""

    override val count: String = didlObject.resources
            ?.takeIf { it.isNotEmpty() }
            ?.get(0)
            ?.duration
            ?.let { duration ->
                duration.split("\\.".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0]
            } ?: ""

    override val icon: Int = R.drawable.ic_action_video
}
