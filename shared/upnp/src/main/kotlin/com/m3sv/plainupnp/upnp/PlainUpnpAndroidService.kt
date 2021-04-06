package com.m3sv.plainupnp.upnp

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.m3sv.plainupnp.core.eventbus.events.ExitApplication
import com.m3sv.plainupnp.core.eventbus.post
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.fourthline.cling.UpnpService
import javax.inject.Inject

@AndroidEntryPoint
class PlainUpnpAndroidService : Service() {

    @Inject
    lateinit var upnpService: UpnpService

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NotificationBuilder.ACTION_EXIT -> {
                MainScope().launch {
                    stopSelf()
                    post(ExitApplication)
                    upnpService.shutdown()
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
            }

            STOP_SERVICE -> stopSelf()

            START_SERVICE -> startForeground(
                NotificationBuilder.SERVER_NOTIFICATION,
                NotificationBuilder(this).buildNotification()
            )
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, PlainUpnpAndroidService::class.java).apply {
                action = START_SERVICE
            }

            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, PlainUpnpAndroidService::class.java).apply {
                action = STOP_SERVICE
            }

            context.startService(intent)
        }

        private const val START_SERVICE = "START_UPNP_SERVICE"
        private const val STOP_SERVICE = "STOP_UPNP_SERVICE"
    }
}
