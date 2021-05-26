package com.m3sv.plainupnp.data.upnp

import org.fourthline.cling.support.model.TransportState

sealed class UpnpRendererState {
    data class Default(
        val uri: String,
        val type: UpnpItemType,
        var state: TransportState,
        val remainingDuration: String,
        val duration: String,
        val position: String,
        val elapsedPercent: Int,
        val durationSeconds: Long,
        val title: String,
        val artist: String?,
    ) : UpnpRendererState()

    object Empty : UpnpRendererState()
}


