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
abstract class MainActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindMainActivityViewModel(mainActivityViewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainFragmentViewModel::class)
    abstract fun bindMainFragmentViewModel(mainFragmentViewModel: MainFragmentViewModel): ViewModel

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contritubeSettingsFragment(): SettingsFragment
}