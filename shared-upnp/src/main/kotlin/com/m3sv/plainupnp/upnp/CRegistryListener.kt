package com.m3sv.plainupnp.upnp

import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry

class CRegistryListener(private val registryListener: RegistryListener) :
    DefaultRegistryListener() {

    override fun remoteDeviceAdded(registry: Registry, device: RemoteDevice) {
        registryListener.deviceAdded(CDevice(device))
    }

    override fun remoteDeviceRemoved(registry: Registry, device: RemoteDevice) {
        registryListener.deviceRemoved(CDevice(device))
    }

    override fun localDeviceAdded(registry: Registry, device: LocalDevice) {
        registryListener.deviceAdded(CDevice(device))
    }

    override fun localDeviceRemoved(registry: Registry, device: LocalDevice) {
        registryListener.deviceRemoved(CDevice(device))
    }
}
