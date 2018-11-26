package com.m3sv.plainupnp.upnp

import android.content.Intent
import com.m3sv.plainupnp.common.ClingExecutor
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UpnpService : AndroidUpnpServiceImpl() {

    override fun createConfiguration(): AndroidUpnpServiceConfiguration =
        object : AndroidUpnpServiceConfiguration() {
            override fun getRegistryMaintenanceIntervalMillis(): Int = 7000
            override fun getSyncProtocolExecutorService(): ExecutorService = com.m3sv.plainupnp.common.ClingExecutor()
//                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.d("Unbind UPnP service")
        return super.onUnbind(intent)
    }
}
