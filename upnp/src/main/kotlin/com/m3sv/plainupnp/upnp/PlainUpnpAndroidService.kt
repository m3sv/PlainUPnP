package com.m3sv.plainupnp.upnp

import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class PlainUpnpAndroidService : AndroidUpnpServiceImpl(), CoroutineScope {

    private val serviceJob = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + serviceJob

    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var notificationBuilder: NotificationBuilder

    private val executor = Executors.newFixedThreadPool(64)

    override fun createConfiguration(): AndroidUpnpServiceConfiguration =
        object : AndroidUpnpServiceConfiguration() {
            override fun getRegistryMaintenanceIntervalMillis(): Int = 7000
            override fun getSyncProtocolExecutorService(): ExecutorService = executor
        }

    override fun onCreate() {
        super.onCreate()
        notificationBuilder = NotificationBuilder(this)
        notificationManager = NotificationManagerCompat.from(this)

        launch {
            val notification = notificationBuilder.buildNotification()

            notificationManager.notify(NotificationBuilder.SERVER_NOTIFICATION, notification)
            startForeground(
                NotificationBuilder.SERVER_NOTIFICATION,
                notification
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (NotificationBuilder.ACTION_EXIT == intent.action) {
                // TODO kill application
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
