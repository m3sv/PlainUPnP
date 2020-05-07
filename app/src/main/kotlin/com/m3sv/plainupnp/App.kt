package com.m3sv.plainupnp

import android.app.Application
import android.os.StrictMode
import com.m3sv.plainupnp.di.AppComponent
import com.m3sv.plainupnp.di.DaggerAppComponent
import com.m3sv.plainupnp.upnp.MediaServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent
            .factory()
            .create(this)

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

        GlobalScope.launch(Dispatchers.IO) {
            MediaServer(this@App).apply { start() }
        }
    }
}
