package com.m3sv.droidupnp.data


data class RendererState(
    val durationRemaining: String?,
    val durationElapse: String?,
    val progress: Int,
    val title: String?,
    val artist: String?,
    val state: UpnpRendererState.State
)