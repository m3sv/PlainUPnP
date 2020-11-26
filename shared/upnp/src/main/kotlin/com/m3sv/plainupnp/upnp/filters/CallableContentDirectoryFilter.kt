package com.m3sv.plainupnp.upnp.filters

import com.m3sv.plainupnp.data.upnp.UpnpDevice

class CallableContentDirectoryFilter : CallableFilter {
    override var device: UpnpDevice? = null

    override fun call(): Boolean? =
        device?.asService("ContentDirectory") ?: false
}
