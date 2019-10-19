package com.m3sv.plainupnp.upnp

import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry

class CRegistryListener(private val registryListener: RegistryListener) : DefaultRegistryListener() {

    /* Discovery performance optimization for very slow Android devices! */
    override fun remoteDeviceDiscoveryStarted(registry: Registry?, device: RemoteDevice?) {
        registryListener.deviceAdded(CDevice(device))
    }

    override fun remoteDeviceDiscoveryFailed(
            registry: Registry?,
            device: RemoteDevice?,
            ex: Exception?
    ) {
        registryListener.deviceRemoved(CDevice(device))
    }

    /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */
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
