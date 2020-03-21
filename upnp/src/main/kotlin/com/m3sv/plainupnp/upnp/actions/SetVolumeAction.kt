package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.UpnpServiceController
import com.m3sv.plainupnp.upnp.UpnpServiceListener
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class SetVolumeAction @Inject constructor(
    listener: UpnpServiceListener,
    controller: UpnpServiceController
) : RenderingAction(listener, controller) {

    suspend operator fun invoke(volume: Int) = suspendCoroutine<Int> { continuation ->
        executeRenderingAction {
            object : SetVolume(this, volume.toLong()) {
                override fun failure(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    operation: UpnpResponse?,
                    defaultMsg: String?
                ) {
                    Timber.e("Failed to raise volume")
                    continuation.resume(volume)
                }

                override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                    super.success(invocation)
                    continuation.resume(volume)
                }
            }
        }
    }

}
