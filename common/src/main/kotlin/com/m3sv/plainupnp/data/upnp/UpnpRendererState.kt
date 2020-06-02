package com.m3sv.plainupnp.data.upnp

import org.fourthline.cling.support.model.TransportState

data class UpnpRendererState(
    val id: String,
    val uri: String?,
    val type: UpnpItemType,
    var state: TransportState,
    val remainingDuration: String?,
    val duration: String?,
    val position: String?,
    val elapsedPercent: Int?,
    val durationSeconds: Long?,
    val title: String,
    val artist: String?
)
