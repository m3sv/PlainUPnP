package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.upnp.actions.*
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import org.fourthline.cling.model.meta.Service
import javax.inject.Inject

class UpnpRepository @Inject constructor(
    private val stop: StopAction,
    private val pause: PauseAction,
    private val play: PlayAction,
    private val setUri: SetUriAction,
    private val seekTo: SeekAction,
    private val getTransportInfo: GetTransportInfoAction,
    private val getPositionInfo: GetPositionInfoAction,
    private val avServiceFinder: AvServiceFinder
) {
    suspend fun play() {
        executeWithAvService { play(this) }
    }

    suspend fun pause() {
        executeWithAvService { pause(this) }
    }

    suspend fun stop() {
        executeWithAvService { stop(this) }
    }

    suspend fun setUri(uri: String, metadata: TrackMetadata) {
        executeWithAvService {
            setUri(this, uri, metadata)
        }
    }

    suspend fun seekTo(time: String) {
        executeWithAvService { seekTo(this, time) }
    }

    suspend fun getTransportInfo() = executeWithAvService {
        getTransportInfo(this)
    }

    suspend fun getPositionInfo() = executeWithAvService {
        getPositionInfo(this)
    }

    private inline fun <T> executeWithAvService(block: Service<*, *>.() -> T?): T? {
        val avService = avServiceFinder.getService() ?: return null
        return block(avService)
    }
}
