package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.ContentRepository
import com.m3sv.plainupnp.common.Filter
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl
import com.m3sv.plainupnp.upnp.android.AndroidUpnpServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.fourthline.cling.UpnpService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BinderModule {

    @Binds
    @Singleton
    abstract fun bindFilterDelegate(filter: Filter): FilterDelegate

    @Binds
    abstract fun bindUpnpService(service: AndroidUpnpServiceImpl): UpnpService

    @Binds
    abstract fun bindContentRepository(contentRepositoryImpl: UpnpContentRepositoryImpl): ContentRepository
}
