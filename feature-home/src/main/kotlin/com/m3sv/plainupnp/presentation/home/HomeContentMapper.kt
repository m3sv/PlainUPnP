package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import com.m3sv.plainupnp.upnp.didl.*
import javax.inject.Inject

class HomeContentMapper @Inject constructor() {
    fun map(items: List<DIDLObjectDisplay>): List<ContentItem> = items.map { item ->
        when (item.didlObject) {
            is ClingDIDLContainer -> ContentItem(
                itemUri = item.didlObject.id,
                name = item.title,
                type = ContentType.FOLDER,
                icon = R.drawable.ic_folder,
                iconUri = item.didlObject.icon
            )

            is ClingImageItem -> ContentItem(
                itemUri = (item.didlObject as ClingDIDLItem).uri,
                name = item.title,
                type = ContentType.IMAGE,
                icon = R.drawable.ic_bordered_image,
                iconUri = item.didlObject.icon
            )

            is ClingVideoItem -> ContentItem(
                itemUri = (item.didlObject as ClingDIDLItem).uri,
                name = item.title,
                type = ContentType.VIDEO,
                icon = R.drawable.ic_bordered_video,
                iconUri = item.didlObject.icon
            )

            is ClingAudioItem -> ContentItem(
                itemUri = (item.didlObject as ClingDIDLItem).uri,
                name = item.title,
                type = ContentType.AUDIO,
                icon = R.drawable.ic_bordered_music,
                iconUri = item.didlObject.icon
            )

            else -> error("Unknown DIDLObject")
        }
    }
}
