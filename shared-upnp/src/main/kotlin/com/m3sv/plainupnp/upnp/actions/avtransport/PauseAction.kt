package com.m3sv.plainupnp.upnp.actions.avtransport

import com.m3sv.plainupnp.upnp.actions.Action
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.Pause
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PauseAction @Inject constructor(controlPoint: ControlPoint) :
    Action<Unit, Boolean>(controlPoint) {

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: Unit
    ): Boolean = suspendCoroutine { continuation ->
        val tag = "AV"
        Timber.tag(tag).d("Pause called")
        val action = object : Pause(service) {
            override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                Timber.tag(tag).d("Pause success")
                continuation.resume(true)
            }

            override fun failure(
                p0: ActionInvocation<out Service<*, *>>?,
                p1: UpnpResponse?,
                p2: String?
            ) {
                Timber.tag(tag).e("Pause failed")
                continuation.resume(false)
            }
        }

        controlPoint.execute(action)
    }


}
