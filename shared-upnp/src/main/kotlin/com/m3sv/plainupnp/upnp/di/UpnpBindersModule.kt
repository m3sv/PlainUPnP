package com.m3sv.plainupnp.upnp.di

import android.content.Context
import com.m3sv.plainupnp.upnp.PlainUpnpServiceConfiguration
import com.m3sv.plainupnp.upnp.android.AndroidUpnpServiceImpl
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import com.m3sv.plainupnp.upnp.manager.UpnpManagerImpl
import com.m3sv.plainupnp.upnp.resourceproviders.LocalServiceResourceProvider
import com.m3sv.plainupnp.upnp.volume.UpnpVolumeManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.fourthline.cling.UpnpService
import org.fourthline.cling.controlpoint.ControlPoint
import javax.inject.Singleton

@Module
abstract class UpnpBindersModule {

    @Binds
    @Singleton
    abstract fun bindUpnpVolumeManager(volumeManagerImpl: UpnpManager): UpnpVolumeManager

    @Binds
    @Singleton
    abstract fun bindUpnpManager(upnpManagerImpl: UpnpManagerImpl): UpnpManager

    companion object {
        @Provides
        @JvmStatic
        @Singleton
        fun provideUpnpService(
            context: Context,
            localServiceResourceProvider: LocalServiceResourceProvider
        ): UpnpService = AndroidUpnpServiceImpl(
            context,
            PlainUpnpServiceConfiguration(),
            localServiceResourceProvider
        )

        @Provides
        @JvmStatic
        @Singleton
        fun provideControlPoint(upnpService: UpnpService): ControlPoint = upnpService.controlPoint
    }
}
