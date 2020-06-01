package com.m3sv.plainupnp.upnp.actions

import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.Play
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PlayAction @Inject constructor(controlPoint: ControlPoint) :
    AvAction<Unit, Boolean>(controlPoint) {

    override suspend operator fun invoke(service: Service<*, *>, vararg arguments: Unit): Boolean =
        suspendCoroutine { continuation ->
            Timber.tag(tag).d("Play called")
            executeAVAction(
                object : Play(service) {
                    override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                        Timber.tag(tag).d("Play success")
                        continuation.resume(true)
                    }

                    override fun failure(
                        p0: ActionInvocation<out Service<*, *>>?,
                        p1: UpnpResponse?,
                        p2: String?
                    ) {
                        Timber.tag(tag).d("Play failed")
                        continuation.resume(false)
                    }
                }
            )
        }

}
