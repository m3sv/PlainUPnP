package com.m3sv.droidupnp

import android.app.Activity
import android.app.Application
import com.m3sv.droidupnp.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject

class App : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder().application(this).build().inject(this)
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }
}
