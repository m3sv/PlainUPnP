package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.ContentRepository
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl
import com.m3sv.plainupnp.upnp.android.AndroidUpnpServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.fourthline.cling.UpnpService

@Module
@InstallIn(SingletonComponent::class)
abstract class BinderModule {

    @Binds
    abstract fun bindUpnpService(service: AndroidUpnpServiceImpl): UpnpService

    @Binds
    abstract fun bindContentRepository(contentRepositoryImpl: UpnpContentRepositoryImpl): ContentRepository
}
