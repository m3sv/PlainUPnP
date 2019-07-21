package com.m3sv.plainupnp.data.upnp

interface UpnpRendererState {

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
        PLAY, PAUSE, STOP, INITIALIZING, FINISHED
    }
}
