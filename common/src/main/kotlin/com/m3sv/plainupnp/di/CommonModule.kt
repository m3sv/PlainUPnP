package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.logging.Log
import com.m3sv.plainupnp.logging.Logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface CommonModule {

    @Binds
    fun bindLogger(logger: Logger): Log
}
