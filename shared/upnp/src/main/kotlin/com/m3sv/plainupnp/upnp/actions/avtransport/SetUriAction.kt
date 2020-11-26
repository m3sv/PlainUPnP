package com.m3sv.plainupnp.upnp.actions.avtransport

import com.m3sv.plainupnp.upnp.actions.Action
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SetUriAction @Inject constructor(controlPoint: ControlPoint) :
    Action<String, Boolean>(controlPoint) {

    suspend operator fun invoke(
        service: Service<*, *>,
        uri: String,
        trackMetadata: TrackMetadata
    ): Boolean = invoke(service, uri, trackMetadata.xml)

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: String
    ): Boolean = suspendCoroutine { continuation ->
        val tag = "AV"
        Timber.tag(tag).d("Set uri: ${arguments[0]}")
        val action = object : SetAVTransportURI(service, arguments[0], arguments[1]) {

            override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                Timber.tag(tag).d("Set uri: ${arguments[0]} success")
                continuation.resume(true)
            }

            override fun failure(
                p0: ActionInvocation<out Service<*, *>>?,
                p1: UpnpResponse?,
                p2: String?
            ) {
                Timber.tag(tag).e("Failed to set uri: ${arguments[0]}")
                continuation.resume(false)
            }
        }
        controlPoint.execute(action)
    }
}
