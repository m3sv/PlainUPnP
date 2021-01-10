package com.m3sv.plainupnp

import android.app.Application
import android.os.StrictMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.m3sv.plainupnp.common.BackgroundModeManager
import com.m3sv.plainupnp.common.util.generateUdn
import com.m3sv.plainupnp.common.util.updateTheme
import com.m3sv.plainupnp.di.AppComponent
import com.m3sv.plainupnp.di.DaggerAppComponent
import com.m3sv.plainupnp.presentation.home.HomeComponent
import com.m3sv.plainupnp.presentation.home.HomeComponentProvider
import com.m3sv.plainupnp.presentation.onboarding.OnboardingActivity
import com.m3sv.plainupnp.presentation.onboarding.OnboardingInjector
import com.m3sv.plainupnp.presentation.settings.SettingsComponent
import com.m3sv.plainupnp.presentation.settings.SettingsComponentProvider
import com.m3sv.plainupnp.upnp.android.AndroidUpnpServiceImpl
import com.m3sv.plainupnp.upnp.di.UpnpSubComponent
import com.m3sv.plainupnp.upnp.di.UpnpSubComponentProvider
import com.m3sv.plainupnp.upnp.server.MediaServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.fourthline.cling.UpnpService
import timber.log.Timber
import javax.inject.Inject

class App : Application(),
    HomeComponentProvider,
    OnboardingInjector,
    SettingsComponentProvider,
    UpnpSubComponentProvider {

    @Inject
    lateinit var server: MediaServer

    @Inject
    lateinit var upnpService: UpnpService

    @Inject
    lateinit var backgroundModeManager: BackgroundModeManager

    val appComponent: AppComponent by lazy {
        DaggerAppComponent
            .factory()
            .create(this)
    }

    override val homeComponent: HomeComponent
        get() = appComponent.homeSubComponent().create()

    override val settingsComponent: SettingsComponent
        get() = appComponent.settingsSubComponent().create()

    override val upnpSubComponent: UpnpSubComponent
        get() = appComponent.upnpSubComponent().create()

    override fun inject(onboardingActivity: OnboardingActivity) {
        appComponent.inject(onboardingActivity)
    }

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)

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

        if (backgroundModeManager.isAllowedToRunInBackground()) {
            (upnpService as AndroidUpnpServiceImpl).resume()
            server.start()
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onMoveToForeground() {
                Timber.d("Starting server")
                GlobalScope.launch(Dispatchers.IO) {
                    if (!backgroundModeManager.isAllowedToRunInBackground()) {
                        (upnpService as AndroidUpnpServiceImpl).resume()
                        server.start()
                    }
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onMoveToBackground() {
                Timber.d("Stopping server")
                GlobalScope.launch(Dispatchers.IO) {
                    if (!backgroundModeManager.isAllowedToRunInBackground()) {
                        (upnpService as AndroidUpnpServiceImpl).pause()
                        server.stop()
                    }
                }
            }
        })
    }
}
