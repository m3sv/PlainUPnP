package com.m3sv.plainupnp.data.upnp

object EmptyUpnpRendererState : UpnpRendererState {
    override val id: String = ""
    override val uri: String? = null
    override val type: UpnpItemType = UpnpItemType.UKNOWN
    override var state: UpnpRendererState.State = UpnpRendererState.State.STOP
    override var volume: Int = 0
    override var isMute: Boolean = false
    override val remainingDuration: String = ""
    override val duration: String = ""
    override val position: String = ""
    override val elapsedPercent: Int = 0
    override val durationSeconds: Long = 0L
    override val title: String = ""
    override val artist: String = ""
}