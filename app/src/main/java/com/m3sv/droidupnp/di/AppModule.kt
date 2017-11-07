package com.m3sv.droidupnp.di

import android.content.Context
import com.m3sv.droidupnp.DroidUPnPApp
import com.m3sv.droidupnp.upnp.UPnPManager
import dagger.Module
import dagger.Provides
import org.droidupnp.controller.upnp.IUPnPServiceController
import org.droidupnp.model.upnp.IFactory


@Module
class AppModule {

    @Provides
    fun provideContext(app: DroidUPnPApp): Context = app.applicationContext

    @Provides
    fun provideUPnPManager(controller: IUPnPServiceController, factory: IFactory) = UPnPManager(controller, factory)
}