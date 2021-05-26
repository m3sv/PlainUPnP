package com.m3sv.plainupnp.upnp.actions.avtransport

import com.m3sv.plainupnp.upnp.actions.Action
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    fun pause(service: Service<*, *>): Flow<Unit> = callbackFlow {
        val tag = "AV"
        Timber.tag(tag).d("Pause called")
        val action = object : Pause(service) {
            override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                Timber.tag(tag).d("Pause success")
                sendBlocking(Unit)
            }

            override fun failure(
                p0: ActionInvocation<out Service<*, *>>?,
                p1: UpnpResponse?,
                p2: String?,
            ) {
                error("Pause failed")
            }
        }

        controlPoint.execute(action)

        awaitClose()
    }

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: Unit,
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
                p2: String?,
            ) {
                Timber.tag(tag).e("Pause failed")
                continuation.resume(false)
            }
        }

        controlPoint.execute(action)
    }


}
