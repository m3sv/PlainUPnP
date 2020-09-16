package com.m3sv.plainupnp

import android.app.Application
import android.os.StrictMode
import com.m3sv.plainupnp.common.util.generateUdn
import com.m3sv.plainupnp.common.util.updateTheme
import com.m3sv.plainupnp.di.AppComponent
import com.m3sv.plainupnp.di.DaggerAppComponent
import com.m3sv.plainupnp.presentation.home.HomeComponent
import com.m3sv.plainupnp.presentation.home.HomeComponentProvider
import com.m3sv.plainupnp.presentation.settings.SettingsComponent
import com.m3sv.plainupnp.presentation.settings.SettingsComponentProvider
import com.m3sv.plainupnp.upnp.server.MediaServer
import timber.log.Timber
import kotlin.concurrent.thread

class App : Application(),
    HomeComponentProvider,
    SettingsComponentProvider {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent
            .factory()
            .create(this)
    }

    override val homeComponent: HomeComponent
        get() = appComponent.homeSubcomponent().create()

    override val settingsComponent: SettingsComponent
        get() = appComponent.settingsSubcomponent().create()

    override fun onCreate() {
        super.onCreate()
        updateTheme()
        generateUdn()

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

        thread {
            MediaServer(this@App).apply { start() }
        }
    }
}
