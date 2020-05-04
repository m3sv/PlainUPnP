package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import com.m3sv.plainupnp.upnp.didl.*
import javax.inject.Inject

class HomeContentMapper @Inject constructor() {
    fun map(items: List<DIDLObjectDisplay>): List<ContentItem> = items.map { item ->
        when (item.didlObject) {
            is ClingDIDLContainer -> {
                ContentItem(
                    item.didlObject.id,
                    item.title,
                    ContentType.FOLDER,
                    icon = R.drawable.ic_folder
                )
            }

            is ClingImageItem -> {
                ContentItem(
                    (item.didlObject as ClingDIDLItem).uri,
                    item.title,
                    ContentType.IMAGE,
                    icon = R.drawable.ic_bordered_image
                )
            }

            is ClingVideoItem -> {
                ContentItem(
                    (item.didlObject as ClingDIDLItem).uri,
                    item.title,
                    ContentType.VIDEO,
                    icon = R.drawable.ic_bordered_video
                )
            }

            is ClingAudioItem -> {
                ContentItem(
                    (item.didlObject as ClingDIDLItem).uri,
                    item.title,
                    ContentType.AUDIO,
                    icon = R.drawable.ic_bordered_music
                )
            }

            else -> throw IllegalStateException("Unknown DIDLObject")
        }
    }
}
