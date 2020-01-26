package com.m3sv.plainupnp.presentation.home

import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
interface HomeFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun bindHomeFragmentViewModel(homeViewModel: HomeViewModel): ViewModel
}
