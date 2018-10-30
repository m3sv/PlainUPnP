package org.droidupnp.legacy.upnp

import com.m3sv.plainupnp.data.UpnpDevice

class CallableContentDirectoryFilter : ICallableFilter {

    private var device: UpnpDevice? = null

    override fun setDevice(device: UpnpDevice) {
        this.device = device
    }

    override fun call(): Boolean? = device?.asService("ContentDirectory") ?: false
}
