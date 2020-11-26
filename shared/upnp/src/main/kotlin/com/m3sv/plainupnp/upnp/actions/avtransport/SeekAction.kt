package com.m3sv.plainupnp.upnp.actions.avtransport

import com.m3sv.plainupnp.upnp.actions.Action
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.Seek
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SeekAction @Inject constructor(controlPoint: ControlPoint) :
    Action<String, Unit>(controlPoint) {

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: String
    ) = suspendCoroutine<Unit> { continuation ->
        val tag = "AV"
        Timber.tag(tag).d("Seek to ${arguments[0]}")
        val action = object : Seek(service, arguments[0]) {
            override fun success(invocation: ActionInvocation<*>?) {
                Timber.tag(tag).v("Seek to ${arguments[0]} success")
                continuation.resume(Unit)
            }

            override fun failure(
                arg0: ActionInvocation<*>,
                arg1: UpnpResponse,
                arg2: String
            ) {
                Timber.tag(tag).e("Seek to ${arguments[0]} failed")
                continuation.resume(Unit)
            }
        }

        controlPoint.execute(action)
    }
}
