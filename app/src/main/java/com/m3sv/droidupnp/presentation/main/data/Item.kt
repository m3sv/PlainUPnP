package com.m3sv.droidupnp.presentation.main.data


data class Item(val thumbnailUri: String, val contentType: ContentType)

enum class ContentType {
    IMAGE, VIDEO, AUDIO
}