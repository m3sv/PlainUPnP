package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.common.UpnpResourceProviderImpl
import com.m3sv.plainupnp.di.scope.ApplicationScope
import com.m3sv.plainupnp.upnp.*
import dagger.Binds
import dagger.Module

@Module
abstract class UPnPBinder {
    @Binds
    @ApplicationScope
    abstract fun bindController(controller: DefaultUpnpServiceController): UpnpServiceController

    @Binds
    @ApplicationScope
    abstract fun bindUpnpStore(upnpStateRepository: UpnpStateRepository): UpnpStateStore

    @Binds
    abstract fun bindUpnpManager(defaultUpnpManager: DefaultUpnpManager): UpnpManager

    @Binds
    abstract fun bindUpnpResourceProvider(context: UpnpResourceProviderImpl): UpnpResourceProvider
}