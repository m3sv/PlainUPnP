package com.m3sv.plainupnp.upnp.discovery.device

import com.m3sv.plainupnp.logging.Log
import com.m3sv.plainupnp.upnp.filters.CallableRendererFilter
import org.fourthline.cling.UpnpService
import javax.inject.Inject

class RendererDiscovery @Inject constructor(upnpService: UpnpService, log: Log) :
    DeviceDiscovery(upnpService = upnpService, log = log) {
    override val callableFilter: CallableFilter = CallableRendererFilter()
}
