package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.CallableFilter

class CallableRendererFilter : CallableFilter {

    override var device: UpnpDevice? = null

    override fun call(): Boolean = device?.asService("RenderingControl") ?: false
}
