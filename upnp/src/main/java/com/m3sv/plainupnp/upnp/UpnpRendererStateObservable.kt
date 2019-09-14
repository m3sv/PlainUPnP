package com.m3sv.plainupnp.upnp


import com.m3sv.plainupnp.data.upnp.UpnpItemType
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import io.reactivex.Observable
import io.reactivex.Observer
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo

class UpnpRendererStateObservable(
    private val itemId: String,
    private val itemUri: String?,
    private val itemType: UpnpItemType
) : Observable<UpnpRendererState>() {

    private var innerState: UpnpInnerState? = null

    val isMute: Boolean
        get() = innerState?.isMute ?: false

    val durationSeconds: Long
        get() = innerState?.durationSeconds ?: 0L

    override fun subscribeActual(observer: Observer<in UpnpRendererState>) {
        innerState =
            UpnpInnerState(
                observer,
                itemId,
                itemUri,
                itemType
            ).also { observer.onSubscribe(it) }
    }

    fun setVolume(volume: Int) {
        innerState?.let { it.volume = volume }
    }

    fun setPositionInfo(positionInfo: PositionInfo) {
        innerState?.setPositionInfo(positionInfo)
    }

    fun setMuted(muted: Boolean) {
        innerState?.let {
            it.isMute = muted
        }
    }

    fun setMediaInfo(mediaInfo: MediaInfo) {
        innerState?.let {
            it.mediaInfo = mediaInfo
        }
    }

    fun setTransportInfo(transportInfo: TransportInfo?) {
        innerState?.let {
            it.transportInfo = transportInfo
        }
    }
}
