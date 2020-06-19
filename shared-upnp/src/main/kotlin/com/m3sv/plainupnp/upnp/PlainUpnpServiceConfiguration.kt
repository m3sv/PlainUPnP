package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.upnp.android.AndroidUpnpServiceConfiguration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PlainUpnpServiceConfiguration : AndroidUpnpServiceConfiguration() {
    override fun getRegistryMaintenanceIntervalMillis(): Int = 7000
    override fun getSyncProtocolExecutorService(): ExecutorService =
        Executors.newFixedThreadPool(64)
}
