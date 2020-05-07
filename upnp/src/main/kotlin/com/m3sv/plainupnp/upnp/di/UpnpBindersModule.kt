package com.m3sv.plainupnp.upnp.di

import android.content.Context
import com.m3sv.plainupnp.upnp.*
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import com.m3sv.plainupnp.upnp.manager.UpnpManagerImpl
import com.m3sv.plainupnp.upnp.manager.UpnpVolumeManager
import com.m3sv.plainupnp.upnp.manager.UpnpVolumeManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.fourthline.cling.UpnpService
import javax.inject.Singleton

@Module
abstract class UpnpBindersModule {

    @Binds
    @Singleton
    abstract fun bindUpnpVolumeManager(volumeManagerImpl: UpnpVolumeManagerImpl): UpnpVolumeManager

    @Binds
    @Singleton
    abstract fun bindController(controller: UpnpServiceControllerImpl): UpnpServiceController

    @Binds
    @Singleton
    abstract fun bindServiceFinder(controller: UpnpServiceControllerImpl): RendererServiceFinder

    @Binds
    @Singleton
    abstract fun bindUpnpStore(upnpStateRepository: UpnpStateRepository): UpnpStateStore

    @Binds
    @Singleton
    abstract fun bindUpnpManager(upnpManagerImpl: UpnpManagerImpl): UpnpManager

    @Binds
    @Singleton
    abstract fun bindUpnpNavigator(upnpNavigator: UpnpNavigatorImpl): UpnpNavigator

    companion object {
        @Provides
        @JvmStatic
        @Singleton
        fun provideUpnpService(context: Context): UpnpService =
            AndroidUpnpServiceImpl(context, PlainUpnpServiceConfiguration())
    }
}
