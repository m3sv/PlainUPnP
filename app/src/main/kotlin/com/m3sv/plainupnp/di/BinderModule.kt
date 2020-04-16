package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.ShutdownNotifier
import com.m3sv.plainupnp.ShutdownNotifierImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class BinderModule {

    companion object {
        @JvmStatic
        @Provides
        @Singleton
        fun bindShutdownNotifier(): ShutdownNotifier = ShutdownNotifierImpl
    }

}
