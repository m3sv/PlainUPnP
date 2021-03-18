package com.m3sv.plainupnp

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.m3sv.plainupnp.common.BackgroundModeManager
import com.m3sv.plainupnp.common.util.generateUdn
import com.m3sv.plainupnp.presentation.main.MainActivity
import com.m3sv.plainupnp.upnp.android.AndroidUpnpServiceImpl
import com.m3sv.plainupnp.upnp.server.MediaServer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.fourthline.cling.UpnpService
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Router {

    @Inject
    lateinit var server: MediaServer

    @Inject
    lateinit var upnpService: UpnpService

    @Inject
    lateinit var backgroundModeManager: BackgroundModeManager

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate() {
        super.onCreate()
        themeManager.setDefaultNightMode()
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

    override fun getNextIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
}
