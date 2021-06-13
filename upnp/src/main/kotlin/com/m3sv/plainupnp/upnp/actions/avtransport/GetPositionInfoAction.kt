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
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo
import org.fourthline.cling.support.model.PositionInfo
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GetPositionInfoAction @Inject constructor(controlPoint: ControlPoint) :
    Action<Unit, PositionInfo?>(controlPoint) {

    fun getPositionInfo(
        service: Service<*, *>,
    ): Flow<PositionInfo> = callbackFlow {
        val tag = "AV"

        Timber.tag(tag).d("Get position info")
        val action = object : GetPositionInfo(service) {
            override fun received(
                invocation: ActionInvocation<out Service<*, *>>?,
                positionInfo: PositionInfo,
            ) {
                Timber.tag(tag).d("Received position info")
                trySendBlocking(positionInfo)
                close()
            }

            override fun failure(
                p0: ActionInvocation<out Service<*, *>>?,
                p1: UpnpResponse?,
                p2: String?,
            ) {
                error("Failed to get position info!")
            }
        }

        controlPoint.execute(action)
        awaitClose()
    }

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: Unit,
    ): PositionInfo? = suspendCoroutine { continuation ->
        val tag = "AV"

        Timber.tag(tag).d("Get position info")
        val action = object : GetPositionInfo(service) {
            override fun received(
                invocation: ActionInvocation<out Service<*, *>>?,
                positionInfo: PositionInfo?,
            ) {
                Timber.tag(tag).d("Received position info")
                continuation.resume(positionInfo)
            }

            override fun failure(
                p0: ActionInvocation<out Service<*, *>>?,
                p1: UpnpResponse?,
                p2: String?,
            ) {
                Timber.tag(tag).e("Failed to get position info")
                continuation.resume(null)
            }
        }

        controlPoint.execute(action)
    }
}
