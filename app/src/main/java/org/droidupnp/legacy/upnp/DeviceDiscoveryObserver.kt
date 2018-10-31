package org.droidupnp.legacy.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent

interface DeviceDiscoveryObserver {
    fun addedDevice(device: UpnpDeviceEvent)

    fun removedDevice(device: UpnpDeviceEvent)
}
