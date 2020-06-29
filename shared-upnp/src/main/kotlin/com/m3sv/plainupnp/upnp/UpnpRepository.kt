package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.upnp.actions.avtransport.*
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import org.fourthline.cling.model.meta.Service
import javax.inject.Inject

class UpnpRepository @Inject constructor(
    private val stopAction: StopAction,
    private val pauseAction: PauseAction,
    private val playAction: PlayAction,
    private val setUriAction: SetUriAction,
    private val seekToAction: SeekAction,
    private val getTransportInfoAction: GetTransportInfoAction,
    private val getPositionInfoAction: GetPositionInfoAction
) {
    suspend fun play(service: Service<*, *>) {
        playAction(service)
    }

    suspend fun pause(service: Service<*, *>) {
        pauseAction(service)
    }

    suspend fun stop(service: Service<*, *>) {
        stopAction(service)
    }

    suspend fun setUri(service: Service<*, *>, uri: String, metadata: TrackMetadata) {
        setUriAction(service, uri, metadata)
    }

    suspend fun seekTo(service: Service<*, *>, time: String) {
        seekToAction(service, time)
    }

    suspend fun getTransportInfo(service: Service<*, *>) = getTransportInfoAction(service)

    suspend fun getPositionInfo(service: Service<*, *>) = getPositionInfoAction(service)

}
