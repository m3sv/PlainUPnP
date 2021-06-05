package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.upnp.actions.avtransport.*
import com.m3sv.plainupnp.upnp.actions.misc.BrowseAction
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import kotlinx.coroutines.flow.Flow
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
import javax.inject.Inject

class UpnpRepository @Inject constructor(
    private val stopAction: StopAction,
    private val pauseAction: PauseAction,
    private val playAction: PlayAction,
    private val setUriAction: SetUriAction,
    private val seekToAction: SeekAction,
    private val getTransportInfoAction: GetTransportInfoAction,
    private val getPositionInfoAction: GetPositionInfoAction,
    private val browseAction: BrowseAction,
) {
    suspend fun browse(
        service: Service<*, *>,
        directoryID: String,
    ) = browseAction(service, directoryID)

    fun browseFlow(
        service: Service<*, *>,
        directoryID: String,
    ): Flow<List<ClingDIDLObject>> = browseAction.browse(service, directoryID)

    fun getPositionInfoFlow(service: Service<*, *>): Flow<PositionInfo> = getPositionInfoAction.getPositionInfo(service)

    fun getTransportInfoFlow(service: Service<*, *>): Flow<TransportInfo> =
        getTransportInfoAction.getTransportInfo(service)

    fun seekToFlow(service: Service<*, *>, time: String): Flow<Unit> {
        return seekToAction.seek(service, time)
    }

    fun stopFlow(service: Service<*, *>): Flow<Unit> {
        return stopAction.stop(service)
    }

    fun pauseFlow(service: Service<*, *>): Flow<Unit> {
        return pauseAction.pause(service)
    }

    fun playFlow(service: Service<*, *>): Flow<Unit> = playAction.play(service)

    fun setUriFlow(service: Service<*, *>, uri: String, metadata: TrackMetadata): Flow<Unit> =
        setUriAction.setUri(service, uri, metadata)

    suspend fun getTransportInfo(service: Service<*, *>): TransportInfo? = getTransportInfoAction(service)

    suspend fun getPositionInfo(service: Service<*, *>) = getPositionInfoAction(service)


}
