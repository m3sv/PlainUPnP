package com.m3sv.plainupnp.upnp

import android.content.Context
import com.m3sv.plainupnp.upnp.android.AndroidRouter
import com.m3sv.plainupnp.upnp.android.AndroidUpnpServiceConfiguration
import com.m3sv.plainupnp.upnp.android.UpnpServiceImpl
import com.m3sv.plainupnp.upnp.resourceproviders.LocalServiceResourceProvider

class AndroidUpnpServiceImpl(
    context: Context,
    androidUpnpServiceConfiguration: AndroidUpnpServiceConfiguration
) : UpnpServiceImpl(androidUpnpServiceConfiguration, context) {

    init {
        val localUpnpDevice = LocalUpnpDevice.getLocalDevice(
            LocalServiceResourceProvider(context),
            context
        )
        registry.addDevice(localUpnpDevice)
        controlPoint.search()
    }

    override fun shutdown() {
        (router as AndroidRouter).unregisterBroadcastReceiver()
        super.shutdown(false)
    }
}
