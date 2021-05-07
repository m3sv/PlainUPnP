package com.m3sv.plainupnp.presentation.settings.ratehandler

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RateHandlerModule {

    @Binds
    @Singleton
    fun bindRateHandler(handler: PlayRateHandler): RateHandler
}
