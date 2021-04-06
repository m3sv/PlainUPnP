package com.m3sv.plainupnp

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import com.m3sv.plainupnp.common.util.generateUdn
import com.m3sv.plainupnp.presentation.main.MainActivity
import com.m3sv.plainupnp.upnp.UpnpScopeProvider
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Router, UpnpScopeProvider {

//    @Inject
//    lateinit var server: MediaServer
//
//    @Inject
//    lateinit var upnpService: UpnpService
//
//    @Inject
//    lateinit var backgroundModeManager: BackgroundModeManager

    @Inject
    lateinit var themeManager: ThemeManager

    override val upnpScope =
        CoroutineScope(SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher())

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

        // TODO investigate background mode
//        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
//            @OnLifecycleEvent(Lifecycle.Event.ON_START)
//            fun onMoveToForeground() {
//                Timber.d("Starting server")
//                upnpScope.launch(Dispatchers.IO) {
//                    if (backgroundModeManager.backgroundMode == BackgroundMode.DENIED) {
//                        (upnpService as AndroidUpnpServiceImpl).resume()
//                        server.start()
//                    }
//                }
//            }
//
//            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//            fun onMoveToBackground() {
//                Timber.d("Stopping server")
//                upnpScope.launch(Dispatchers.IO) {
//                    if (backgroundModeManager.backgroundMode == BackgroundMode.DENIED) {
//                        (upnpService as AndroidUpnpServiceImpl).pause()
//                        server.stop()
//                    }
//                }
//            }
//        })
    }

    override fun getNextIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
}
