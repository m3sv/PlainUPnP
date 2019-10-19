package com.m3sv.plainupnp.data.upnp

interface UpnpRendererState {

    val id: String

    val uri: String?

    val type: UpnpItemType

    var state: State

    var volume: Int

    var isMute: Boolean

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
