package com.m3sv.plainupnp.di

import android.content.Context
import com.m3sv.plainupnp.presentation.home.HomeComponent
import com.m3sv.plainupnp.presentation.main.di.MainActivitySubComponent
import com.m3sv.plainupnp.presentation.settings.SettingsComponent
import com.m3sv.plainupnp.upnp.di.UpnpBindersModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        UpnpBindersModule::class,
        BinderModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun mainSubcomponent(): MainActivitySubComponent.Factory
    fun homeSubcomponent(): HomeComponent.Factory
    fun settingsSubcomponent(): SettingsComponent.Factory
}
