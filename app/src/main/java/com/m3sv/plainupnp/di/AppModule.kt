package com.m3sv.plainupnp.di

import android.app.Application
import android.content.Context
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
    fun provideUPnPManager(factory: Factory, upnpNavigator: UpnpNavigator) =
            DefaultUpnpManager(factory.upnpServiceController, factory,
                    upnpNavigator,
                    RendererDiscoveryObservable(factory.upnpServiceController),
                    ContentDirectoryDiscoveryObservable(factory.upnpServiceController))

    @Provides
    @JvmStatic
    @ApplicationScope
    fun provideServiceListener(context: Context): UpnpServiceListener = UpnpServiceListener(context)
}