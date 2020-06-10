package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.ShutdownNotifier
import com.m3sv.plainupnp.ShutdownNotifierImpl
import com.m3sv.plainupnp.common.Filter
import com.m3sv.plainupnp.common.FilterDelegate
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class BinderModule {

    @Binds
    @Singleton
    abstract fun bindFilterDelegate(filter: Filter): FilterDelegate

    companion object {
        @JvmStatic
        @Provides
        @Singleton
        fun bindShutdownNotifier(): ShutdownNotifier = ShutdownNotifierImpl
    }

}
