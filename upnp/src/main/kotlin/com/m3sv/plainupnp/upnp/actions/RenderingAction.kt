package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.CDevice
import com.m3sv.plainupnp.upnp.UpnpServiceController
import org.fourthline.cling.UpnpService
import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType

abstract class RenderingAction(
    private val upnpService: UpnpService,
    private val controller: UpnpServiceController
) {
    protected fun executeRenderingAction(callback: Service<*, *>.() -> ActionCallback) {
        controller.selectedRenderer
            ?.let { (it as CDevice).device?.findService(UDAServiceType("RenderingControl")) }
            ?.let { service -> upnpService.controlPoint.execute(callback(service)) }
    }
}
