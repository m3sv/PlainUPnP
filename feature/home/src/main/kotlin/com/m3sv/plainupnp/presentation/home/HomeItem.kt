package com.m3sv.plainupnp.presentation.home

import androidx.annotation.DrawableRes

data class ContentItem(
    val itemUri: String,
    val name: String,
    val type: ContentType,
    @DrawableRes val icon: Int,
    val userGenerated: Boolean = false,
)

enum class ContentType {
    IMAGE, AUDIO, VIDEO, FOLDER
}
