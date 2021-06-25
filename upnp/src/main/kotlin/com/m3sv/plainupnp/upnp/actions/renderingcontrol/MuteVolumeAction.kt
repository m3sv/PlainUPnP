package com.m3sv.plainupnp.upnp.actions.renderingcontrol

import com.m3sv.plainupnp.logging.Log
import com.m3sv.plainupnp.upnp.actions.Action
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.renderingcontrol.callback.SetMute
import javax.inject.Inject

class MuteVolumeAction @Inject constructor(
    controlPoint: ControlPoint,
    private val log: Log
) :
    Action<Unit, Unit>(controlPoint) {

    // Don't know what is happening here, but Kotlin compiler complains about this
    // Put empty method here, figure out this later
    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: Unit
    ) {
        error("Use method with Boolean arguments")
    }

    suspend operator fun invoke(
        service: Service<*, *>,
        vararg arguments: Boolean
    ) {
        val action = object : SetMute(service, arguments[0]) {
            override fun failure(
                invocation: ActionInvocation<out Service<*, *>>?,
                operation: UpnpResponse?,
                defaultMsg: String?
            ) {
                log.e("Failed to mute volume")
            }
        }
        controlPoint.execute(action)
    }
}
