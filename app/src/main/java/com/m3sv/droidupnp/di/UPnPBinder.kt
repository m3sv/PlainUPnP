package com.m3sv.droidupnp.di

import dagger.Binds
import dagger.Module
import com.m3sv.droidupnp.upnp.ServiceController
import com.m3sv.droidupnp.upnp.UPnPFactory
import com.m3sv.droidupnp.upnp.UpnpServiceController
import org.droidupnp.legacy.upnp.Factory

@Module
abstract class UPnPBinder {
    @Binds
    abstract fun bindFactory(UPnPFactory: UPnPFactory): Factory

    @Binds
    abstract fun bindController(controller: ServiceController): UpnpServiceController
}