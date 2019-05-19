package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.upnp.*
import dagger.Binds
import dagger.Module

@Module
abstract class UPnPBinder {
    @Binds
    abstract fun bindFactory(UpnpFactory: UpnpFactory): Factory

    @Binds
    abstract fun bindController(controller: ServiceController): UpnpServiceController

    @Binds
    abstract fun bindUpnpNavigator(upnpNavigator: DefaultUpnpNavigator): UpnpNavigator
}