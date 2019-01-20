package com.m3sv.plainupnp.data.upnp


data class RendererState(
    val durationRemaining: String? = null,
    val durationElapse: String? = null,
    val progress: Int,
    val title: String? = null,
    val artist: String? = null,
    val state: UpnpRendererState.State
)