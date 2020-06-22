package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice

interface RegistryListener {

    fun deviceAdded(device: UpnpDevice)

    fun deviceRemoved(device: UpnpDevice)

}
