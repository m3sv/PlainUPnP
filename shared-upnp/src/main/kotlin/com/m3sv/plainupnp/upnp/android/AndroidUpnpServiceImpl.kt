package com.m3sv.plainupnp.upnp.android

import android.content.Context
import com.m3sv.plainupnp.upnp.LocalUpnpDevice
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
        controlPoint.search(30)
    }

    override fun shutdown() {
        (router as AndroidRouter).unregisterBroadcastReceiver()
        super.shutdown(false)
    }
}
