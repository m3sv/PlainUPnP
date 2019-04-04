package com.m3sv.plainupnp.di

import dagger.Binds
import dagger.Module
import com.m3sv.plainupnp.upnp.ServiceController
import com.m3sv.plainupnp.upnp.UpnpFactory
import com.m3sv.plainupnp.upnp.UpnpServiceController
import com.m3sv.plainupnp.upnp.navigator.DefaultUpnpNavigator
import com.m3sv.plainupnp.upnp.navigator.UpnpNavigator
import org.droidupnp.legacy.upnp.Factory

@Module
abstract class UPnPBinder {
    @Binds
    abstract fun bindFactory(UpnpFactory: UpnpFactory): Factory

    @Binds
    abstract fun bindController(controller: ServiceController): UpnpServiceController

    @Binds
    abstract fun bindUpnpNavigator(upnpNavigator: DefaultUpnpNavigator): UpnpNavigator
}