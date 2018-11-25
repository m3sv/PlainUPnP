package com.m3sv.plainupnp.upnp

import android.content.Intent
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import timber.log.Timber

class UpnpService : AndroidUpnpServiceImpl() {

    override fun createConfiguration(): AndroidUpnpServiceConfiguration =
        object : AndroidUpnpServiceConfiguration() {
            override fun getRegistryMaintenanceIntervalMillis(): Int = 7000
        }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.d("Unbind UPnP service")
        return super.onUnbind(intent)
    }
}
