package com.m3sv.plainupnp.upnp.actions.avtransport

import com.m3sv.plainupnp.upnp.actions.Action
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo
import org.fourthline.cling.support.model.TransportInfo
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GetTransportInfoAction @Inject constructor(controlPoint: ControlPoint) :
    Action<Unit, TransportInfo?>(controlPoint) {

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: Unit
    ): TransportInfo? = suspendCoroutine { continuation ->
        val tag = "AV"
        Timber.tag(tag).d("Get transport info")

        val action = object : GetTransportInfo(service) {
            override fun received(
                invocation: ActionInvocation<out Service<*, *>>?,
                transportInfo: TransportInfo?
            ) {
                Timber.tag(tag).d("Received transport info")
                continuation.resume(transportInfo)
            }

            override fun failure(
                p0: ActionInvocation<out Service<*, *>>?,
                p1: UpnpResponse?,
                p2: String?
            ) {
                Timber.tag(tag).e("Failed to get transport info")
                continuation.resume(null)
            }
        }

        controlPoint.execute(action)
    }
}

