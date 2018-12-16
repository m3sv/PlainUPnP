package com.m3sv.plainupnp.upnp.didl

import com.m3sv.plainupnp.R
import org.fourthline.cling.support.model.item.AudioItem
import org.fourthline.cling.support.model.item.MusicTrack

class ClingAudioItem(item: AudioItem) : ClingDIDLItem(item) {

    override fun getDataType(): String = "audio/*"

    override fun getDescription(): String {
        if (didlObject is MusicTrack) {
            val track = didlObject
            return (track.firstArtist?.name?.let { it } ?: "") +
                    (track.album?.let { " - $it" } ?: "")
        }
        return (didlObject as AudioItem).description
    }

    override fun getCount(): String = didlObject.resources
        ?.takeIf { it.isNotEmpty() }
        ?.get(0)
        ?.duration
        ?.let { duration ->
            duration
                .split("\\.".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()[0]
        } ?: ""

    override fun getIcon(): Int = R.drawable.ic_action_headphones
}
