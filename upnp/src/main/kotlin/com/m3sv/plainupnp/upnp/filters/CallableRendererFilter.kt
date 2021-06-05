package com.m3sv.plainupnp.upnp.filters

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.discovery.device.CallableFilter

class CallableRendererFilter : CallableFilter {

    override var device: UpnpDevice? = null

    override fun call(): Boolean = device?.asService("RenderingControl") ?: false
}
