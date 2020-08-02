package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.common.Filter
import com.m3sv.plainupnp.common.FilterDelegate
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class BinderModule {

    @Binds
    @Singleton
    abstract fun bindFilterDelegate(filter: Filter): FilterDelegate

}
