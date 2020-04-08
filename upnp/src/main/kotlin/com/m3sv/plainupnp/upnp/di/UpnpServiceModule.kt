package com.m3sv.plainupnp.upnp.di

import android.content.Context
import com.m3sv.plainupnp.upnp.AndroidUpnpServiceImpl
import com.m3sv.plainupnp.upnp.PlainUpnpServiceConfiguration
import dagger.Module
import dagger.Provides
import org.fourthline.cling.UpnpService
import javax.inject.Singleton

@Module
object UpnpServiceModule {

    @Provides
    @JvmStatic
    @Singleton
    fun provideUpnpService(
        context: Context
    ): UpnpService = AndroidUpnpServiceImpl(context, PlainUpnpServiceConfiguration())
}
