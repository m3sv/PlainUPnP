package com.m3sv.plainupnp.upnp.di

import com.m3sv.plainupnp.upnp.manager.UpnpManager
import com.m3sv.plainupnp.upnp.manager.UpnpManagerImpl
import com.m3sv.plainupnp.upnp.volume.UpnpVolumeManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.fourthline.cling.UpnpService
import org.fourthline.cling.controlpoint.ControlPoint
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UpnpBindersModule {

    @Binds
    @Singleton
    abstract fun bindUpnpVolumeManager(volumeManagerImpl: UpnpManager): UpnpVolumeManager

    @Binds
    @Singleton
    abstract fun bindUpnpManager(upnpManagerImpl: UpnpManagerImpl): UpnpManager

    companion object {
        @Provides
        @Singleton
        fun provideControlPoint(upnpService: UpnpService): ControlPoint = upnpService.controlPoint
    }
}
