package com.m3sv.plainupnp.upnp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class PlainUpnpAndroidService : Service() {

    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var notificationBuilder: NotificationBuilder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager = NotificationManagerCompat.from(this)

        when (intent?.action) {
            NotificationBuilder.ACTION_EXIT -> {
                stopForeground(false)
                notificationManager.cancelAll()
                stopSelf(startId)
                finishChannel.offer(Unit)
            }

            START_SERVICE -> {
                notificationBuilder = NotificationBuilder(this)
                val notification = notificationBuilder.buildNotification()

                startForeground(
                    NotificationBuilder.SERVER_NOTIFICATION,
                    notification
                )
            }
        }

        return START_STICKY
    }

    fun inject() {
        // TODO inject upnp manager here
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val START_SERVICE = "START_UPNP_SERVICE"

        private val finishChannel = BroadcastChannel<Unit>(1)

        val finishFlow: Flow<Unit> = finishChannel.asFlow()
    }
}
