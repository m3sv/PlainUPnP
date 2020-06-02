package com.m3sv.plainupnp.upnp.actions

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
    AvAction<String, Unit>(controlPoint) {

    override suspend operator fun invoke(service: Service<*, *>, vararg arguments: String) =
        suspendCoroutine<Unit> { continuation ->
            Timber.tag(tag).d("Seek to ${arguments[0]}")
            executeAVAction(
                object : Seek(service, arguments[0]) {
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
                })
        }

}
