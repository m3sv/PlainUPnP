package org.droidupnp.model.upnp

import com.m3sv.droidupnp.data.UpnpDeviceEvent

interface DeviceDiscoveryObserver {
    fun addedDevice(device: UpnpDeviceEvent)

    fun removedDevice(device: UpnpDeviceEvent)
}
