package com.m3sv.plainupnp.upnp.actions.avtransport

import com.m3sv.plainupnp.upnp.actions.Action
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.Stop
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StopAction @Inject constructor(controlPoint: ControlPoint) :
    Action<Any, Boolean>(controlPoint) {

    fun stop(service: Service<*, *>): Flow<Unit> = callbackFlow {
        val tag = "AV"
        Timber.tag(tag).d("Stop called")
        val action = object : Stop(service) {
            override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                Timber.tag(tag).d("Stop success")
                trySendBlocking(Unit)
                close()
            }

            override fun failure(
                p0: ActionInvocation<out Service<*, *>>?,
                p1: UpnpResponse?,
                p2: String?,
            ) {
                error("Stop failed!")
            }
        }

        controlPoint.execute(action)

        awaitClose()
    }

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: Any,
    ): Boolean = suspendCoroutine { continuation ->
        val tag = "AV"
        Timber.tag(tag).d("Stop called")
        val action = object : Stop(service) {
            override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                Timber.tag(tag).d("Stop success")
                continuation.resume(true)
            }

            override fun failure(
                p0: ActionInvocation<out Service<*, *>>?,
                p1: UpnpResponse?,
                p2: String?,
            ) {
                Timber.tag(tag).e("Stop failed")
                continuation.resume(false)
            }
        }

        controlPoint.execute(action)
    }
}
