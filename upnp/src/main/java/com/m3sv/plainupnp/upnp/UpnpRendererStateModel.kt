package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpRendererState


data class UpnpRendererStateModel(
        val state: UpnpRendererState.State,
        val remainingDuration: String?,
        val elapsedDuration: String?,
        val progress: Int,
        val title: String?,
        val artist: String?,
        val volume: Int,
        val mute: Boolean
)