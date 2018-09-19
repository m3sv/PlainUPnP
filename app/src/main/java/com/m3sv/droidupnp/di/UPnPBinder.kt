package com.m3sv.droidupnp.di

import dagger.Binds
import dagger.Module
import org.droidupnp.controller.cling.UPnPFactory
import org.droidupnp.controller.cling.ServiceController
import org.droidupnp.controller.upnp.UpnpServiceController
import org.droidupnp.model.upnp.Factory

@Module
abstract class UPnPBinder {
    @Binds
    abstract fun bindFactory(UPnPFactory: UPnPFactory) : Factory

    @Binds
    abstract fun bindController(controller: ServiceController) : UpnpServiceController
}