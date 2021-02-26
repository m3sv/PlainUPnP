package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl
import com.m3sv.plainupnp.upnp.didl.ClingContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.didl.ClingMedia
import javax.inject.Inject

class ClingContentMapper @Inject constructor() {
    fun map(items: List<ClingDIDLObject>): List<ContentItem> = items.map { item ->
        when (item) {
            is ClingContainer -> handleContainer(item)

            is ClingMedia.Image -> ContentItem(
                itemUri = requireNotNull(item.uri),
                name = item.title,
                type = ContentType.IMAGE,
                icon = R.drawable.ic_bordered_image,
                clingItem = item
            )

            is ClingMedia.Video -> ContentItem(
                itemUri = requireNotNull(item.uri),
                name = item.title,
                type = ContentType.VIDEO,
                icon = R.drawable.ic_bordered_video,
                clingItem = item
            )

            is ClingMedia.Audio -> ContentItem(
                itemUri = requireNotNull(item.uri),
                name = item.title,
                type = ContentType.AUDIO,
                icon = R.drawable.ic_bordered_music,
                clingItem = item
            )

            else -> error("Unknown DIDLObject")
        }
    }

    private fun handleContainer(item: ClingDIDLObject): ContentItem {
        return when {
            item.title.startsWith(UpnpContentRepositoryImpl.USER_DEFINED_PREFIX) -> ContentItem(
                itemUri = item.id,
                name = item.title.replace(UpnpContentRepositoryImpl.USER_DEFINED_PREFIX, ""),
                type = ContentType.USER_SELECTED_FOLDER,
                icon = R.drawable.ic_folder,
                userSelected = true,
                clingItem = item
            )
            else -> ContentItem(
                itemUri = item.id,
                name = item.title,
                type = ContentType.FOLDER,
                icon = R.drawable.ic_folder,
                userSelected = false,
                clingItem = item
            )
        }
    }
}
