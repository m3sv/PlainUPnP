package com.m3sv.plainupnp.di

import android.app.Application
import android.content.Context
import com.m3sv.plainupnp.di.scope.ApplicationScope
import com.m3sv.plainupnp.upnp.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.UpnpNavigator
import dagger.Module
import dagger.Provides
import com.m3sv.plainupnp.upnp.Factory


@Module
internal object AppModule {

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideContext(app: Application): Context = app.applicationContext

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideUPnPManager(context: Context, factory: Factory, upnpNavigator: UpnpNavigator) =
            DefaultUpnpManager(factory.upnpServiceController, factory,
                    upnpNavigator,
                    RendererDiscoveryObservable(context, factory.upnpServiceController),
                    ContentDirectoryDiscoveryObservable(factory.upnpServiceController))
}