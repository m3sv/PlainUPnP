package com.m3sv.plainupnp.di

import android.app.Application
import android.content.Context
import com.m3sv.plainupnp.di.scope.ApplicationScope
import com.m3sv.plainupnp.upnp.DefaultUpnpManager
import com.m3sv.plainupnp.upnp.discovery.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.discovery.RendererDiscoveryObservable
import dagger.Module
import dagger.Provides
import org.droidupnp.legacy.upnp.Factory


@Module
internal object AppModule {

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideContext(app: Application): Context = app.applicationContext

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideUPnPManager(context: Context, factory: Factory) =
            DefaultUpnpManager(factory.upnpServiceController, factory,
                    RendererDiscoveryObservable(context, factory.upnpServiceController),
                    ContentDirectoryDiscoveryObservable(factory.upnpServiceController))
}