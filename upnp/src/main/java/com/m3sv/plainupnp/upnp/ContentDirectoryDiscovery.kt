package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice

class ContentDirectoryDiscovery(controller: UpnpServiceController) : DeviceDiscovery(controller) {

    override val callableFilter: CallableFilter = CallableContentDirectoryFilter()

    override fun isSelected(device: UpnpDevice): Boolean =
        controller.selectedContentDirectory?.equals(device) ?: false

    override fun select(device: UpnpDevice) {
        select(device, false)
    }

    override fun select(device: UpnpDevice, force: Boolean) {
        controller.setSelectedContentDirectory(device, force)
    }

    override fun removed(device: UpnpDevice) {
        if (controller.selectedContentDirectory != null && device.equals(controller.selectedContentDirectory))
            controller.selectedContentDirectory = null
    }
}
