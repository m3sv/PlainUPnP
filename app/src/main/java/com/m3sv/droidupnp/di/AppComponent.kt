package com.m3sv.droidupnp.di

import android.app.Application
import com.m3sv.droidupnp.App
import com.m3sv.droidupnp.di.activity.MainActivityBuilder
import com.m3sv.droidupnp.di.scope.ApplicationScope
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@ApplicationScope
@Component(
    modules = [AndroidSupportInjectionModule::class,
        MainActivityBuilder::class,
        AppModule::class,
        UPnPBinder::class]
)
interface AppComponent : AndroidInjector<App> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    override fun inject(instance: App)
}