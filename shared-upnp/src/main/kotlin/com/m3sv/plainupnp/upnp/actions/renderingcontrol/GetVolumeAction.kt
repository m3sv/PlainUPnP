package com.m3sv.plainupnp.upnp.actions.renderingcontrol

import com.m3sv.plainupnp.upnp.actions.Action
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class GetVolumeAction @Inject constructor(controlPoint: ControlPoint) :
    Action<Unit, Int>(controlPoint) {

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: Unit
    ): Int = suspendCoroutine { continuation ->
        val action = object : GetVolume(service) {
            override fun failure(
                invocation: ActionInvocation<out Service<*, *>>?,
                operation: UpnpResponse?,
                defaultMsg: String?
            ) {
                continuation.resume(0)
            }

            override fun received(
                actionInvocation: ActionInvocation<out Service<*, *>>?,
                currentVolume: Int
            ) {
                continuation.resume(currentVolume)
            }
        }

        controlPoint.execute(action)
    }
}
