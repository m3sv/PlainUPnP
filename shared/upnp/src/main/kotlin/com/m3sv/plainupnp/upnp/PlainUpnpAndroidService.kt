package com.m3sv.plainupnp.upnp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.m3sv.plainupnp.core.eventbus.events.ExitApplication
import com.m3sv.plainupnp.core.eventbus.post
import com.m3sv.plainupnp.upnp.di.UpnpSubComponentProvider
import org.fourthline.cling.UpnpService
import javax.inject.Inject

class PlainUpnpAndroidService : Service() {

    @Inject
    lateinit var upnpService: UpnpService

    override fun onCreate() {
        super.onCreate()
        inject()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NotificationBuilder.ACTION_EXIT -> {
                stopForeground(false)
                stopSelf(startId)
            }

            START_SERVICE -> startForeground(
                NotificationBuilder.SERVER_NOTIFICATION,
                NotificationBuilder(this).buildNotification()
            )
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        post(ExitApplication)
        upnpService.shutdown()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    private fun inject() {
        (application as UpnpSubComponentProvider).upnpSubComponent.inject(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val START_SERVICE = "START_UPNP_SERVICE"
    }
}
