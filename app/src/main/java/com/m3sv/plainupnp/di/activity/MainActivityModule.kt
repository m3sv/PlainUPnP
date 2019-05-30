package com.m3sv.plainupnp.di.activity

import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.di.ViewModelKey
import com.m3sv.plainupnp.di.scope.ActivityScope
import com.m3sv.plainupnp.presentation.main.MainActivityViewModel
import com.m3sv.plainupnp.presentation.main.MainFragment
import com.m3sv.plainupnp.presentation.main.MainFragmentViewModel
import com.m3sv.plainupnp.presentation.settings.SettingsFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
interface MainActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    fun bindMainActivityViewModel(mainActivityViewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainFragmentViewModel::class)
    fun bindMainFragmentViewModel(mainFragmentViewModel: MainFragmentViewModel): ViewModel

    @ActivityScope
    @ContributesAndroidInjector
    fun contributeMainFragment(): MainFragment

    @ActivityScope
    @ContributesAndroidInjector
    fun contritubeSettingsFragment(): SettingsFragment
}