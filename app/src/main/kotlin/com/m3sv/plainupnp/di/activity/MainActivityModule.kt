package com.m3sv.plainupnp.di.activity

import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.di.ViewModelKey
import com.m3sv.plainupnp.di.scope.ActivityScope
import com.m3sv.plainupnp.presentation.home.HomeFragment
import com.m3sv.plainupnp.presentation.home.HomeViewModel
import com.m3sv.plainupnp.presentation.main.MainViewModel
import com.m3sv.plainupnp.presentation.settings.SettingsFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
interface MainActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainActivityViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun bindMainFragmentViewModel(homeViewModel: HomeViewModel): ViewModel

    @ActivityScope
    @ContributesAndroidInjector
    fun contributeMainFragment(): HomeFragment

    @ActivityScope
    @ContributesAndroidInjector
    fun contritubeSettingsFragment(): SettingsFragment
}