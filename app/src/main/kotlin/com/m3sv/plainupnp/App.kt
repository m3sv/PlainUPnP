package com.m3sv.plainupnp

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import com.m3sv.plainupnp.di.AppComponent
import com.m3sv.plainupnp.di.ApplicationProviderModule
import com.m3sv.plainupnp.di.DaggerAppComponent
import com.m3sv.plainupnp.upnp.MediaServer
import com.m3sv.plainupnp.upnp.PlainUpnpAndroidService
import timber.log.Timber

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent
            .builder()
            .applicationProviderModule(ApplicationProviderModule(this))
            .build()

        val intent = Intent(this, PlainUpnpAndroidService::class.java).apply {
            action = PlainUpnpAndroidService.START_SERVICE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        val mediaServer = MediaServer(this).apply { start() }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            StrictMode.setThreadPolicy(
                StrictMode
                    .ThreadPolicy
                    .Builder()
                    .detectAll()
                    .build()
            )
        }
    }
}
