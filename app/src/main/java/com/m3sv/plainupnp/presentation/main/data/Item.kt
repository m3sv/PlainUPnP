package com.m3sv.plainupnp.presentation.main.data

import androidx.annotation.DrawableRes
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import com.m3sv.plainupnp.upnp.didl.*

data class Item(
        val uri: String?,
        val name: String,
        val type: ContentType,
        @DrawableRes val icon: Int
)

enum class ContentType {
    IMAGE, AUDIO, VIDEO, DIRECTORY
}