package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.upnp.*
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class UPnPBinder {
    @Binds
    @Singleton
    abstract fun bindController(controller: DefaultUpnpServiceController): UpnpServiceController

    @Binds
    @Singleton
    abstract fun bindUpnpStore(upnpStateRepository: UpnpStateRepository): UpnpStateStore

    @Binds
    abstract fun bindUpnpManager(upnpManagerImpl: UpnpManagerImpl): UpnpManager
}
