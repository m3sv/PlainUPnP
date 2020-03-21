package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.CDevice
import com.m3sv.plainupnp.upnp.UpnpServiceController
import com.m3sv.plainupnp.upnp.UpnpServiceListener
import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType

abstract class RenderingAction(
    private val listener: UpnpServiceListener,
    private val controller: UpnpServiceController
) {
    protected fun executeRenderingAction(callback: Service<*, *>.() -> ActionCallback) {
        controller.selectedRenderer
            ?.let { (it as CDevice).device?.findService(UDAServiceType("RenderingControl")) }
            ?.let { service -> listener.controlPoint?.execute(callback(service)) }
    }
}
