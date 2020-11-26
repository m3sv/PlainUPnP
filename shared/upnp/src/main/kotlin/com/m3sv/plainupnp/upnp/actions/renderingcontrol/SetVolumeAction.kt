package com.m3sv.plainupnp.upnp.actions.renderingcontrol

import com.m3sv.plainupnp.upnp.actions.Action
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class SetVolumeAction @Inject constructor(controlPoint: ControlPoint) :
    Action<Unit, Unit>(controlPoint) {

    // Don't know what is happening here, but Kotlin compiler complains about this
    // Put empty method here, figure out this later
    override suspend fun invoke(service: Service<*, *>, vararg arguments: Unit) {
        error("Use method with Int arguments")
    }

    suspend operator fun invoke(service: Service<*, *>, vararg arguments: Int): Int =
        suspendCoroutine<Int> { continuation ->
            val volume = arguments[0]

            val action = object : SetVolume(service, volume.toLong()) {
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

            controlPoint.execute(action)
        }
}
