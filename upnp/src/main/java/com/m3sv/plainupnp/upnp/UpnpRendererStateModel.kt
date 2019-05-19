package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo


data class UpnpRendererStateModel(
    val state: UpnpRendererState.State,
    val remainingDuration: String?,
    val elapsedDuration: String?,
    val progress: Int,
    val title: String?,
    val artist: String?,
    val volume: Int,
    val mute: Boolean,
    val positionInfo: PositionInfo?,
    val mediaInfo: MediaInfo?,
    val transportInfo: TransportInfo?
)