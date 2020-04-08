package com.m3sv.plainupnp.upnp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import kotlin.system.exitProcess

class PlainUpnpAndroidService : Service() {

    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var notificationBuilder: NotificationBuilder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (NotificationBuilder.ACTION_EXIT == intent?.action) {
            stopForeground(true)
            stopSelf()
            exitProcess(0)
        }

        if (START_SERVICE == intent?.action) {
            notificationBuilder = NotificationBuilder(this)
            notificationManager = NotificationManagerCompat.from(this)

            val notification = notificationBuilder.buildNotification()
            notificationManager.notify(NotificationBuilder.SERVER_NOTIFICATION, notification)

            startForeground(
                NotificationBuilder.SERVER_NOTIFICATION,
                notification
            )
        }

        return START_NOT_STICKY
    }


    fun inject() {
        // TODO inject upnp manager here
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val START_SERVICE = "START_UPNP_SERVICE"
    }
}
