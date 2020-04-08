package com.m3sv.plainupnp.upnp

import android.content.Context
import com.m3sv.plainupnp.upnp.cling.AndroidRouter
import com.m3sv.plainupnp.upnp.cling.AndroidUpnpServiceConfiguration
import com.m3sv.plainupnp.upnp.cling.UpnpServiceImpl
import com.m3sv.plainupnp.upnp.resourceproviders.LocalServiceResourceProvider

class AndroidUpnpServiceImpl(
    context: Context,
    androidUpnpServiceConfiguration: AndroidUpnpServiceConfiguration
) : UpnpServiceImpl(androidUpnpServiceConfiguration, context) {

    init {
        controlPoint.search()
        val localUpnpDevice = LocalUpnpDevice.getLocalDevice(
            LocalServiceResourceProvider(context),
            context
        )
        registry.addDevice(localUpnpDevice)
    }

    override fun shutdown() {
        (router as AndroidRouter).unregisterBroadcastReceiver()
        super.shutdown(true)
    }
}
