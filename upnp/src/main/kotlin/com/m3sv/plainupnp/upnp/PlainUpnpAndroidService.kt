package com.m3sv.plainupnp.upnp

import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PlainUpnpAndroidService : AndroidUpnpServiceImpl() {

    private val executor = Executors.newFixedThreadPool(64)

    override fun createConfiguration(): AndroidUpnpServiceConfiguration =
            object : AndroidUpnpServiceConfiguration() {
                override fun getRegistryMaintenanceIntervalMillis(): Int = 7000
                override fun getSyncProtocolExecutorService(): ExecutorService = executor
            }

    override fun onCreate() {
        super.onCreate()
        Timber.d("Create the service")


    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Destroy the service")
    }

}
