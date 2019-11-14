package com.m3sv.plainupnp.di

import android.app.Application
import android.content.Context
import com.m3sv.plainupnp.ContentCache
import com.m3sv.plainupnp.di.scope.ApplicationScope
import com.m3sv.plainupnp.upnp.*
import com.m3sv.plainupnp.upnp.cleanslate.UpnpServiceListener
import com.m3sv.plainupnp.upnp.resourceproviders.UpnpResourceProvider
import com.m3sv.plainupnp.upnp.usecase.LaunchLocallyUseCase
import dagger.Module
import dagger.Provides


@Module
internal object AppModule {

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideUpnpNavigator(
        upnpServiceController: UpnpServiceController,
        upnpStateStore: UpnpStateStore
    ): UpnpNavigator = DefaultUpnpNavigator(upnpServiceController, upnpStateStore)

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideContext(app: Application): Context = app.applicationContext

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideUPnPManager(
        upnpServiceController: UpnpServiceController,
        upnpNavigator: UpnpNavigator,
        contentCache: ContentCache,
        launchLocallyUseCase: LaunchLocallyUseCase,
        upnpStateStore: UpnpStateStore,
        upnpResourceProvider: UpnpResourceProvider
    ) =
        UpnpManagerImpl(
            RendererDiscoveryObservable(upnpServiceController, upnpResourceProvider),
            ContentDirectoryDiscoveryObservable(upnpServiceController),
            upnpServiceController,
            upnpNavigator,
            launchLocallyUseCase,
            upnpStateStore
        )

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideServiceListener(
        context: Context,
        contentCache: ContentCache
    ) =
        UpnpServiceListener(
            context,
            MediaServer(context),
            contentCache
        )

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideCache(): ContentCache = ContentCache()
}
