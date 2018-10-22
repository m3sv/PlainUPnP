package com.m3sv.plainupnp.presentation.main.data

import com.m3sv.plainupnp.upnp.DIDLObjectDisplay
import org.droidupnp.legacy.cling.didl.ClingAudioItem
import org.droidupnp.legacy.cling.didl.ClingDIDLContainer
import org.droidupnp.legacy.cling.didl.ClingImageItem
import org.droidupnp.legacy.cling.didl.ClingVideoItem

data class Item(
    val uri: String,
    val name: String,
    val type: ContentType,
    val didlObjectDisplay: List<DIDLObjectDisplay>? = null,
    val parentId: String? = null
) {
    companion object {
        fun fromDIDLObjectDisplay(objects: List<DIDLObjectDisplay>?) =
            objects?.map {
                when (it.didlObject) {
                    is ClingDIDLContainer -> {
                        Item(
                            it.didlObject.id,
                            it.title,
                            ContentType.DIRECTORY,
                            objects,
                            it.didlObject.parentID
                        )
                    }

                    is ClingImageItem -> {
                        Item(
                            (it.didlObject as ClingImageItem).uri,
                            it.title,
                            ContentType.IMAGE,
                            objects
                        )
                    }

                    is ClingVideoItem -> {
                        Item(
                            (it.didlObject as ClingVideoItem).uri,
                            it.title,
                            ContentType.VIDEO,
                            objects
                        )
                    }

                    is ClingAudioItem -> {
                        Item(
                            (it.didlObject as ClingAudioItem).uri,
                            it.title,
                            ContentType.AUDIO,
                            objects
                        )
                    }

                    else -> throw IllegalStateException("Unknown DIDLObject")
                }
            } ?: listOf()
    }
}

enum class ContentType {
    IMAGE, AUDIO, VIDEO, DIRECTORY
}