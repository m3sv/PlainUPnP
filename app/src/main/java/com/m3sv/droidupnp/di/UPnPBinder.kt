package com.m3sv.droidupnp.di

import dagger.Binds
import dagger.Module
import org.droidupnp.controller.cling.Factory
import org.droidupnp.controller.cling.ServiceController
import org.droidupnp.controller.upnp.IUPnPServiceController
import org.droidupnp.model.upnp.IFactory

@Module
abstract class UPnPBinder {
    @Binds
    abstract fun bindFactory(factory: Factory) : IFactory

    @Binds
    abstract fun bindController(controller: ServiceController) : IUPnPServiceController
}