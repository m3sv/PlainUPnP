package com.m3sv.plainupnp.presentation.home

import androidx.annotation.DrawableRes
import java.net.URI

data class ContentItem(
    val itemUri: String?,
    val name: String,
    val type: ContentType,
    @DrawableRes val icon: Int,
    val iconUri: URI?
)

enum class ContentType {
    IMAGE, AUDIO, VIDEO, FOLDER
}
