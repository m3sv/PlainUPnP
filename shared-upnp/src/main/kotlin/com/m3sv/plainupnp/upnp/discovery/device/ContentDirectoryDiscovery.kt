package com.m3sv.plainupnp.upnp.discovery.device

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.UpnpServiceController
import com.m3sv.plainupnp.upnp.filters.CallableContentDirectoryFilter
import com.m3sv.plainupnp.upnp.filters.CallableFilter
import org.fourthline.cling.UpnpService

class ContentDirectoryDiscovery(
    controller: UpnpServiceController,
    upnpService: UpnpService
) : DeviceDiscovery(controller, upnpService = upnpService) {

    override val callableFilter: CallableFilter =
        CallableContentDirectoryFilter()

    override fun isSelected(device: UpnpDevice): Boolean =
        controller.selectedContentDirectory?.equals(device) ?: false

    override fun select(device: UpnpDevice) {
        select(device, false)
    }

    override fun select(device: UpnpDevice, force: Boolean) {
        controller.setSelectedContentDirectory(device, force)
    }

    override fun removed(device: UpnpDevice) {
        if (controller.selectedContentDirectory == device)
            controller.selectedContentDirectory = null
    }
}
