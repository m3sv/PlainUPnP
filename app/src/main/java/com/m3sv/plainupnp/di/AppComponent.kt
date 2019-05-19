package com.m3sv.plainupnp.di

import android.app.Application
import com.m3sv.plainupnp.App
import com.m3sv.plainupnp.di.activity.MainActivityBuilder
import com.m3sv.plainupnp.di.activity.TvActivityBuilder
import com.m3sv.plainupnp.di.scope.ApplicationScope
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@ApplicationScope
@Component(
        modules = [AndroidSupportInjectionModule::class,
            MainActivityBuilder::class,
            TvActivityBuilder::class,
            AppModule::class, UPnPBinder::class
        ]
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