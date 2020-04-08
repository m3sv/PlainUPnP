package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.UpnpServiceController
import org.fourthline.cling.UpnpService
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class GetVolumeAction @Inject constructor(
    service: UpnpService,
    controller: UpnpServiceController
) : RenderingAction(service, controller) {

    suspend operator fun invoke(): Int = suspendCoroutine { continuation ->
        executeRenderingAction {
            object : GetVolume(this) {
                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    continuation.resumeWithException(UpnpActionException("Failed to GET volume"))
                }

                override fun received(
                    actionInvocation: ActionInvocation<out Service<*, *>>?,
                    currentVolume: Int
                ) {
                    continuation.resume(currentVolume)
                }
            }
        }
    }

}
