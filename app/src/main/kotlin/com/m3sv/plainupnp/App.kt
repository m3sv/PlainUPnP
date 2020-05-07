package com.m3sv.plainupnp

import android.app.Application
import android.os.StrictMode
import com.m3sv.plainupnp.di.AppComponent
import com.m3sv.plainupnp.di.AppComponentProvider
import com.m3sv.plainupnp.di.DaggerAppComponent
import com.m3sv.plainupnp.presentation.home.HomeComponent
import com.m3sv.plainupnp.presentation.home.HomeComponentProvider
import com.m3sv.plainupnp.upnp.MediaServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class App : Application(), AppComponentProvider, HomeComponentProvider {

    override val appComponent: AppComponent by lazy {
        DaggerAppComponent
            .factory()
            .create(this)
    }

    override val homeComponent: HomeComponent
        get() = appComponent.homeSubcomponent().create()

    override fun onCreate() {
        super.onCreate()

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
