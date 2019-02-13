package com.m3sv.plainupnp.di

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module


@Module
interface ViewModelModule {

    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}