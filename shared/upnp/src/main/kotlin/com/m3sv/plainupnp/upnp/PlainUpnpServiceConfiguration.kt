package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.upnp.android.AndroidUpnpServiceConfiguration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PlainUpnpServiceConfiguration(private val threadPool: ExecutorService = Executors.newFixedThreadPool(32)) :
    AndroidUpnpServiceConfiguration() {
    override fun getRegistryMaintenanceIntervalMillis(): Int = 7000
    override fun getDefaultExecutorService(): ExecutorService = threadPool
}
