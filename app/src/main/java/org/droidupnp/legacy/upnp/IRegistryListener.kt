package org.droidupnp.legacy.upnp

import com.m3sv.plainupnp.data.UpnpDevice

interface IRegistryListener {

    fun deviceAdded(device: UpnpDevice)

    fun deviceRemoved(device: UpnpDevice)
}
