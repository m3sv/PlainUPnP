package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.di.activity.MainActivitySubComponent
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        SubComponentsModule::class,
        AppModule::class,
        UPnPBinder::class,
        ApplicationProviderModule::class
    ]
)
interface AppComponent {
    fun mainActivitySubComponent(): MainActivitySubComponent.Factory
}
