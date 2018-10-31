package org.droidupnp.legacy.cling.didl

import com.m3sv.plainupnp.R

import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.item.VideoItem

class ClingVideoItem(item: VideoItem) : ClingDIDLItem(item) {

    override fun getDataType(): String = "video/*"

    override fun getDescription(): String = didlObject.resources
        ?.takeIf { it.isNotEmpty() }
        ?.get(0)
        ?.resolution ?: ""

    override fun getCount(): String =
        didlObject.resources
            ?.takeIf { it.isNotEmpty() }
            ?.get(0)
            ?.duration
            ?.let { duration ->
                duration.split("\\.".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0]
            } ?: ""

    override fun getIcon(): Int = R.drawable.ic_action_video
}
