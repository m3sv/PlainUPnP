package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.network.ApiManager
import com.m3sv.plainupnp.network.DefaultApiManager
import dagger.Binds
import dagger.Module


@Module
interface NetworkBindingModule {
    @Binds
    fun bindApiManager(defaultApiManager: DefaultApiManager): ApiManager
}