package com.m3sv.droidupnp.di

import com.m3sv.droidupnp.DroidUPnPApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

// todo create custom scope later
@Singleton
@Component(modules = arrayOf(
        AndroidSupportInjectionModule::class,
        AppModule::class,
        BuildersModule::class,
        UPnPBinder::class))
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: DroidUPnPApp): Builder

        fun build(): AppComponent
    }

    fun inject(application: DroidUPnPApp)
}