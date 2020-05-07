package com.m3sv.plainupnp.di

import android.content.Context
import android.content.SharedPreferences
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.di.main.MainActivitySubComponent
import com.m3sv.plainupnp.presentation.base.ControlsSheetDelegate
import com.m3sv.plainupnp.presentation.home.HomeComponent
import com.m3sv.plainupnp.presentation.settings.SettingsComponent
import com.m3sv.plainupnp.upnp.UpnpStateStore
import com.m3sv.plainupnp.upnp.di.UpnpBindersModule
import com.m3sv.plainupnp.upnp.manager.UpnpManager
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

    fun upnpManager(): UpnpManager
    fun filterDelegate(): FilterDelegate
    fun upnpStateStore(): UpnpStateStore
    fun controlsSheetDelegate(): ControlsSheetDelegate
    fun context(): Context
    fun sharedPreferences(): SharedPreferences
}
