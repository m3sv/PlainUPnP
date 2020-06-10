package com.m3sv.plainupnp.presentation.main.di

import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.di.ViewModelKey
import com.m3sv.plainupnp.presentation.main.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
interface MainActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainActivityViewModel(mainViewModel: MainViewModel): ViewModel

}
