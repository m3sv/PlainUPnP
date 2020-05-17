package com.m3sv.plainupnp.data.upnp

import kotlinx.coroutines.flow.Flow

interface UpnpRendererState {

    val flow: Flow<UpnpRendererState>

    val id: String

    val uri: String?

    val type: UpnpItemType

    var state: State

    val remainingDuration: String

    val duration: String

    val position: String

    val elapsedPercent: Int

    val durationSeconds: Long

    val title: String

    val artist: String

    enum class State {
        PLAY,
        PAUSE,
        STOP,
        INITIALIZING,
        FINISHED
    }
}
