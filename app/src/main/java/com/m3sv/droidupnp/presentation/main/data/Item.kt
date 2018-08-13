package com.m3sv.droidupnp.presentation.main.data

data class Item(val uri: String, val name: String, val type: ContentType)

enum class ContentType {
    IMAGE, SOUND, VIDEO
}