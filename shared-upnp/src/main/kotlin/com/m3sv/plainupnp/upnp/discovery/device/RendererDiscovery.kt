package com.m3sv.plainupnp.upnp.discovery.device

import com.m3sv.plainupnp.upnp.filters.CallableFilter
import com.m3sv.plainupnp.upnp.filters.CallableRendererFilter
import org.fourthline.cling.UpnpService
import javax.inject.Inject

class RendererDiscovery @Inject constructor(upnpService: UpnpService) :
    DeviceDiscovery(upnpService = upnpService) {
    override val callableFilter: CallableFilter = CallableRendererFilter()
}
