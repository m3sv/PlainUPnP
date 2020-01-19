package com.m3sv.plainupnp.di

import android.app.Application
import android.content.Context
import com.m3sv.plainupnp.ContentCache
import com.m3sv.plainupnp.upnp.*
import com.m3sv.plainupnp.upnp.cleanslate.UpnpServiceListener
import com.m3sv.plainupnp.upnp.resourceproviders.UpnpResourceProvider
import com.m3sv.plainupnp.upnp.usecase.LaunchLocallyUseCase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
object AppModule {

    @Provides
    @JvmStatic
    fun provideUpnpNavigator(
        upnpServiceController: UpnpServiceController,
        upnpStateStore: UpnpStateStore
    ): UpnpNavigator = UpnpNavigatorImpl(upnpServiceController, upnpStateStore)

    @Provides
    @JvmStatic
    @Singleton
    fun provideContext(app: Application): Context = app.applicationContext

    @Provides
    @JvmStatic
    @Singleton
    fun provideUPnPManager(
        upnpServiceController: UpnpServiceController,
        upnpNavigator: UpnpNavigator,
        launchLocallyUseCase: LaunchLocallyUseCase,
        upnpStateStore: UpnpStateStore,
        upnpResourceProvider: UpnpResourceProvider
    ) =
        UpnpManagerImpl(
            RendererDiscoveryObservable(upnpServiceController, upnpResourceProvider),
            ContentDirectoryDiscoveryObservable(upnpServiceController),
            upnpServiceController,
            launchLocallyUseCase,
            upnpStateStore,
            upnpNavigator
        )

    @Provides
    @JvmStatic
    @Singleton
    fun provideServiceListener(
        context: Context,
        mediaServer: MediaServer,
        contentCache: ContentCache
    ) =
        UpnpServiceListener(
            context,
            mediaServer,
            contentCache
        )
}
