package com.m3sv.plainupnp

import android.app.Application
import android.os.StrictMode
import com.m3sv.plainupnp.di.AppComponent
import com.m3sv.plainupnp.di.ApplicationProviderModule
import com.m3sv.plainupnp.di.DaggerAppComponent
import timber.log.Timber

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent
            .builder()
            .applicationProviderModule(ApplicationProviderModule(this))
            .build()

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
