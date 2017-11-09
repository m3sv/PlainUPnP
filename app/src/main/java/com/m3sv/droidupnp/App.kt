package com.m3sv.droidupnp

import android.app.Activity
import android.app.Application
import com.m3sv.droidupnp.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import timber.log.Timber
import javax.inject.Inject

class App : Application(), HasActivityInjector {

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingAndroidInjector

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder().application(this).build().inject(this)
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }
}
