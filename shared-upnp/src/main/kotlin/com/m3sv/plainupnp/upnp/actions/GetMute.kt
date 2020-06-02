package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.RendererServiceFinder
import org.fourthline.cling.UpnpService
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.renderingcontrol.callback.GetMute
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GetMute @Inject constructor(
    upnpService: UpnpService,
    serviceFinder: RendererServiceFinder
) : RenderingAction(upnpService, serviceFinder) {

    suspend operator fun invoke(): Boolean = suspendCoroutine { continuation ->
        executeRenderingAction {
            object : GetMute(this) {
                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    continuation.resumeWithException(UpnpActionException("Failed to GET volume"))
                }

                override fun received(
                    actionInvocation: ActionInvocation<out Service<*, *>>?,
                    currentMute: Boolean
                ) {
                    continuation.resume(currentMute)
                }
            }
        }
    }

}
