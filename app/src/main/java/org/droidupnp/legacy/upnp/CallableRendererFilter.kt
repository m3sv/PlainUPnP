package org.droidupnp.legacy.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice

class CallableRendererFilter : CallableFilter {

    override var device: UpnpDevice? = null

    override fun call(): Boolean = device?.asService("RenderingControl") ?: false
}
