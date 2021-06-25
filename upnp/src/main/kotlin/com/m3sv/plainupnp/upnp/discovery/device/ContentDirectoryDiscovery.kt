package com.m3sv.plainupnp.upnp.discovery.device

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.logging.Log
import org.fourthline.cling.UpnpService
import javax.inject.Inject

class CallableContentDirectoryFilter : CallableFilter {
    override var device: UpnpDevice? = null

    override fun call(): Boolean? =
        device?.asService("ContentDirectory") ?: false
}

class ContentDirectoryDiscovery @Inject constructor(upnpService: UpnpService, log: Log) :
    DeviceDiscovery(upnpService = upnpService, log = log) {

    override val callableFilter: CallableFilter = CallableContentDirectoryFilter()
}

