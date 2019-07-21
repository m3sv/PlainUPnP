package com.m3sv.plainupnp

import com.m3sv.plainupnp.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber

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
