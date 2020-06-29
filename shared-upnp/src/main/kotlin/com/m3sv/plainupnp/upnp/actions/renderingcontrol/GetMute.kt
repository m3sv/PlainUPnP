package com.m3sv.plainupnp.upnp.actions.renderingcontrol

import com.m3sv.plainupnp.upnp.actions.Action
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.renderingcontrol.callback.GetMute
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GetMute @Inject constructor(controlPoint: ControlPoint) :
    Action<Unit, Boolean>(controlPoint) {

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: Unit
    ): Boolean = suspendCoroutine { continuation ->
        val action = object : GetMute(service) {
            override fun received(
                actionInvocation: ActionInvocation<out Service<*, *>>?,
                currentMute: Boolean
            ) {
                continuation.resume(currentMute)
            }

            override fun failure(
                invocation: ActionInvocation<out Service<*, *>>?,
                operation: UpnpResponse?,
                defaultMsg: String?
            ) {
                continuation.resume(false)
            }
        }

        controlPoint.execute(action)
    }


}
