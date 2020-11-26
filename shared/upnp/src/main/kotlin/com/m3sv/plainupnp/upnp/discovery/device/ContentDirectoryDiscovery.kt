package com.m3sv.plainupnp.upnp.discovery.device

import com.m3sv.plainupnp.upnp.filters.CallableContentDirectoryFilter
import com.m3sv.plainupnp.upnp.filters.CallableFilter
import org.fourthline.cling.UpnpService
import javax.inject.Inject

class ContentDirectoryDiscovery @Inject constructor(upnpService: UpnpService) :
    DeviceDiscovery(upnpService = upnpService) {

    override val callableFilter: CallableFilter = CallableContentDirectoryFilter()
}
