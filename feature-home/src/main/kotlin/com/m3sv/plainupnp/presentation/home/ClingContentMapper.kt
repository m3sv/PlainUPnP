package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.upnp.didl.*
import javax.inject.Inject

class ClingContentMapper @Inject constructor() {
    fun map(items: List<ClingDIDLObject>): List<ContentItem> = items.map { item ->
        when (item) {
            is ClingDIDLContainer -> ContentItem(
                itemUri = item.id,
                name = item.title,
                type = ContentType.FOLDER,
                icon = R.drawable.ic_folder
            )

            is ClingImageItem -> ContentItem(
                itemUri = item.uri,
                name = item.title,
                type = ContentType.IMAGE,
                icon = R.drawable.ic_bordered_image
            )

            is ClingVideoItem -> ContentItem(
                itemUri = item.uri,
                name = item.title,
                type = ContentType.VIDEO,
                icon = R.drawable.ic_bordered_video
            )

            is ClingAudioItem -> ContentItem(
                itemUri = item.uri,
                name = item.title,
                type = ContentType.AUDIO,
                icon = R.drawable.ic_bordered_music
            )

            else -> error("Unknown DIDLObject")
        }
    }
}
