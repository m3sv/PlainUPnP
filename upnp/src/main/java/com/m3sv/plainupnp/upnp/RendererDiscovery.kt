package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import org.fourthline.cling.registry.event.DeviceDiscovery

class RendererDiscovery(controller: UpnpServiceController) : DeviceDiscovery(controller) {

    override val callableFilter: CallableFilter = CallableRendererFilter()

    override fun isSelected(device: UpnpDevice): Boolean =
            controller.selectedRenderer?.equals(controller.selectedRenderer) ?: false

    override fun select(device: UpnpDevice) {
        select(device, false)
    }

    override fun select(device: UpnpDevice, force: Boolean) {
        controller.setSelectedRenderer(device, force)
    }

    override fun removed(device: UpnpDevice) {
        if (controller.selectedRenderer != null && device.equals(controller.selectedRenderer))
            controller.selectedRenderer = null
    }
}
