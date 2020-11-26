package com.m3sv.plainupnp.upnp.android

import android.content.Context
import com.m3sv.plainupnp.upnp.getLocalDevice
import com.m3sv.plainupnp.upnp.resourceproviders.LocalServiceResourceProvider

class AndroidUpnpServiceImpl(
    context: Context,
    configuration: AndroidUpnpServiceConfiguration,
    resourceProvider: LocalServiceResourceProvider
) : UpnpServiceImpl(configuration, context) {

    init {
        registry.addDevice(getLocalDevice(resourceProvider, context))
        controlPoint.search()
    }

    override fun shutdown() {
        (router as AndroidRouter).unregisterBroadcastReceiver()
        super.shutdown(false)
    }
}
