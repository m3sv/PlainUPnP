package com.m3sv.plainupnp.upnp

import android.content.Intent
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UpnpService : AndroidUpnpServiceImpl() {

    override fun createConfiguration(): AndroidUpnpServiceConfiguration =
        object : AndroidUpnpServiceConfiguration() {
            override fun getRegistryMaintenanceIntervalMillis(): Int = 7000
            override fun getSyncProtocolExecutorService(): ExecutorService =
                Executors.newFixedThreadPool(16)
        }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.d("Unbind UPnP service")
        return super.onUnbind(intent)
    }
}
