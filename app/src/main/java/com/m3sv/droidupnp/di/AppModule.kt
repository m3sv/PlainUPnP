package com.m3sv.droidupnp.di

import android.app.Application
import android.content.Context
import com.m3sv.droidupnp.common.Toastable
import com.m3sv.droidupnp.common.Toaster
import com.m3sv.droidupnp.di.scope.ApplicationScope
import com.m3sv.droidupnp.upnp.DefaultUpnpManager
import dagger.Module
import dagger.Provides
import org.droidupnp.legacy.upnp.Factory


@Module
@ApplicationScope
internal object AppModule {

    @Provides
    @JvmStatic
    fun provideContext(app: Application): Context = app.applicationContext

    @Provides
    @ApplicationScope
    @JvmStatic
    fun provideToaster(application: Application): Toastable = Toaster(application)

    @Provides
    @ApplicationScope
    @JvmStatic
    fun provideUPnPManager(context: Context, factory: Factory) =
        DefaultUpnpManager(factory.createUpnpServiceController(context), factory)
}