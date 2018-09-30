package com.m3sv.droidupnp.di

import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module


@Module
interface ViewModelModule {

    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}