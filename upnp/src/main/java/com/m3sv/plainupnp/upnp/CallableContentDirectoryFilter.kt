package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.CallableFilter

class CallableContentDirectoryFilter : CallableFilter {
    override var device: UpnpDevice? = null

    override fun call(): Boolean? = device?.asService("ContentDirectory") ?: false
}
