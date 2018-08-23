package com.m3sv.droidupnp.presentation.main.data

import com.m3sv.droidupnp.upnp.DIDLObjectDisplay

data class Item(
    val uri: String,
    val name: String,
    val type: ContentType,
    val didlObjectDisplay: List<DIDLObjectDisplay>? = null
) {
    companion object {
        fun fromDIDLObjectDisplay(objects: List<DIDLObjectDisplay>) =
            objects.map { Item(it.didlObject.id, it.title, ContentType.DIRECTORY, objects) }
    }
}

enum class ContentType {
    IMAGE, SOUND, VIDEO, DIRECTORY
}