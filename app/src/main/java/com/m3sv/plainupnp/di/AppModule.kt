package com.m3sv.plainupnp.di

import android.app.Application
import android.content.Context
import com.m3sv.plainupnp.ContentCache
import com.m3sv.plainupnp.di.scope.ApplicationScope
import com.m3sv.plainupnp.upnp.*
import com.m3sv.plainupnp.upnp.cleanslate.UpnpServiceListener
import dagger.Module
import dagger.Provides


@Module
internal object AppModule {

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideContext(app: Application): Context = app.applicationContext

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideUPnPManager(factory: Factory,
                           upnpNavigator: UpnpNavigator,
                           contentCache: ContentCache) = DefaultUpnpManager(
            RendererDiscoveryObservable(factory.upnpServiceController),
            ContentDirectoryDiscoveryObservable(factory.upnpServiceController),
            factory.upnpServiceController,
            factory,
            upnpNavigator,
            contentCache)

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideServiceListener(context: Context,
                               contentCache: ContentCache) =
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