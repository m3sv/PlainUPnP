package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.CDevice
import com.m3sv.plainupnp.upnp.UpnpServiceController
import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType

internal abstract class AvAction(
    private val controller: UpnpServiceController,
    private val controlPoint: ControlPoint
) {

    private fun getAVTransportService(): Service<*, *>? =
        controller.selectedRenderer?.let {
            (it as CDevice).device?.findService(UDAServiceType("AVTransport"))
        }

    inline fun executeAVAction(callback: (Service<*, *>) -> ActionCallback) {
        getAVTransportService()?.let { service ->
            controlPoint.execute(callback.invoke(service))
        }
    }

}
