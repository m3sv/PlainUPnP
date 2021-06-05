package com.m3sv.plainupnp.upnp.discovery.device

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import org.fourthline.cling.UpnpService
import javax.inject.Inject

class CallableContentDirectoryFilter : CallableFilter {
    override var device: UpnpDevice? = null

    override fun call(): Boolean? =
        device?.asService("ContentDirectory") ?: false
}

class ContentDirectoryDiscovery @Inject constructor(upnpService: UpnpService) :
    DeviceDiscovery(upnpService = upnpService) {

    override val callableFilter: CallableFilter = CallableContentDirectoryFilter()
}

