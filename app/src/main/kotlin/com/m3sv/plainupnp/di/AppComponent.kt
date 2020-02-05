package com.m3sv.plainupnp.di

import android.content.Context
import android.content.SharedPreferences
import com.m3sv.plainupnp.di.main.MainActivitySubComponent
import com.m3sv.plainupnp.presentation.base.ControlsSheetDelegate
import com.m3sv.plainupnp.presentation.main.FilterDelegate
import com.m3sv.plainupnp.upnp.UpnpManager
import com.m3sv.plainupnp.upnp.UpnpStateStore
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
    fun upnpManager(): UpnpManager
    fun filterDelegate(): FilterDelegate
    fun upnpStateStore(): UpnpStateStore
    fun controlsSheetDelegate(): ControlsSheetDelegate
    fun context(): Context
    fun sharedPreferences(): SharedPreferences
}
