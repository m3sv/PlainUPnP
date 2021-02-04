package com.m3sv.plainupnp.presentation.home

import androidx.annotation.DrawableRes
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject

data class ContentItem(
    val itemUri: String,
    val name: String,
    val type: ContentType,
    @DrawableRes val icon: Int,
    val userSelected: Boolean = false,
    val clingItem: ClingDIDLObject,
)

enum class ContentType {
    FOLDER, USER_SELECTED_FOLDER, IMAGE, AUDIO, VIDEO,
}
