package com.m3sv.plainupnp.upnp.discovery.device

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.UpnpServiceController
import com.m3sv.plainupnp.upnp.filters.CallableFilter
import com.m3sv.plainupnp.upnp.filters.CallableRendererFilter
import org.fourthline.cling.UpnpService

class RendererDiscovery(
    controller: UpnpServiceController,
    upnpService: UpnpService
) : DeviceDiscovery(controller, upnpService = upnpService) {

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
        if (controller.selectedRenderer == device)
            controller.selectedRenderer = null
    }
}
