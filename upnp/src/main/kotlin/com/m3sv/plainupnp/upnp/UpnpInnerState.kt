package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.common.utils.formatTime
import com.m3sv.plainupnp.data.upnp.UpnpItemType
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.model.TransportState

@ExperimentalCoroutinesApi
class UpnpInnerState constructor(
    override val id: String,
    override val uri: String?,
    override val type: UpnpItemType
) : UpnpRendererState {

    private val flowChannel = BroadcastChannel<UpnpInnerState>(Channel.CONFLATED)

    override val flow: Flow<UpnpRendererState> = flowChannel.asFlow()

    init {
        resetTrackInfo()
    }

    override var state: UpnpRendererState.State = UpnpRendererState.State.INITIALIZING
        set(value) {
            field = value
            updateInnerState()
        }

    override var volume: Int = -1
        set(value) {
            field = value
            updateInnerState()
        }

    override var isMute: Boolean = false
        set(value) {
            field = value
            updateInnerState()
        }

    var positionInfo: PositionInfo = PositionInfo()
        set(value) {
            field = value
            updateInnerState()
        }

    var mediaInfo: MediaInfo = MediaInfo()

    var transportInfo: TransportInfo = TransportInfo()
        set(value) {
            state = when (transportInfo.currentTransportState) {
                TransportState.PAUSED_PLAYBACK,
                TransportState.PAUSED_RECORDING -> {
                    UpnpRendererState.State.PAUSE
                }
                TransportState.PLAYING -> UpnpRendererState.State.PLAY
                else -> UpnpRendererState.State.STOP
            }

            field = value
        }

    override val remainingDuration: String
        get() {
            val t: Long = positionInfo.trackRemainingSeconds
            val h = t / 3600
            val m = (t - h * 3600) / 60
            val s = t - h * 3600 - m * 60
            return "-" + formatTime(h.toInt(), m.toInt(), s)
        }

    override val duration: String
        get() {
            val t = positionInfo.trackDurationSeconds
            val h = t / 3600
            val m = (t - h * 3600) / 60
            val s = t - h * 3600 - m * 60
            return formatTime(h.toInt(), m.toInt(), s)
        }

    override val position: String
        get() {
            val t = positionInfo.trackElapsedSeconds
            val h = t / 3600
            val m = (t - h * 3600) / 60
            val s = t - h * 3600 - m * 60
            return formatTime(h.toInt(), m.toInt(), s)
        }

    override val elapsedPercent: Int
        get() = positionInfo.elapsedPercent

    override val durationSeconds: Long
        get() = positionInfo.trackDurationSeconds

    override val title: String
        get() = getTrackMetadata().title ?: ""

    override val artist: String
        get() = getTrackMetadata().artist ?: ""

    private fun getTrackMetadata(): TrackMetadata = TrackMetadata(positionInfo.trackMetaData)

    private fun resetTrackInfo() {
        positionInfo = PositionInfo()
        mediaInfo = MediaInfo()
    }

    private fun updateInnerState() {
        flowChannel.offer(this)
    }

}
