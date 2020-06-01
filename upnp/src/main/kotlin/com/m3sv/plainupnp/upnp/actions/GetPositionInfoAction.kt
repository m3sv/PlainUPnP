package com.m3sv.plainupnp.upnp.actions

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
    AvAction<Unit, PositionInfo?>(controlPoint) {

    override suspend fun invoke(service: Service<*, *>, vararg arguments: Unit): PositionInfo? =
        suspendCoroutine { continuation ->
            Timber.tag(tag).d("Get position info")
            executeAVAction(object : GetPositionInfo(service) {
                override fun received(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    positionInfo: PositionInfo?
                ) {
                    Timber.tag(tag).d("Received position info")
                    continuation.resume(positionInfo)
                }

                override fun failure(
                    p0: ActionInvocation<out Service<*, *>>?,
                    p1: UpnpResponse?,
                    p2: String?
                ) {
                    Timber.tag(tag).e("Failed to get position info")
                    continuation.resume(null)
                }
            })
        }
}
