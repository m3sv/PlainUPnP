package com.m3sv.plainupnp.presentation.upnp

import androidx.annotation.DrawableRes

data class Item(
        val uri: String?,
        val name: String,
        val type: ContentType,
        @DrawableRes val icon: Int
)

enum class ContentType {
    IMAGE, AUDIO, VIDEO, DIRECTORY
}