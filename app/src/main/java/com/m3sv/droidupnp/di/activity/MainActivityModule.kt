package com.m3sv.droidupnp.di.activity

import android.arch.lifecycle.ViewModel
import android.support.v7.app.AppCompatActivity
import com.m3sv.droidupnp.di.ViewModelKey
import com.m3sv.droidupnp.presentation.main.MainActivity
import com.m3sv.droidupnp.presentation.main.MainActivityViewModel
import com.m3sv.droidupnp.presentation.main.MainFragment
import com.m3sv.droidupnp.presentation.main.MainFragmentViewModel
import com.m3sv.droidupnp.presentation.settings.SettingsFragment
import com.m3sv.droidupnp.upnp.DefaultUpnpManager
import com.m3sv.droidupnp.upnp.UpnpManager
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
interface MainActivityModule {
    @Binds
    fun provideAppCompatActivity(mainActivity: MainActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    fun bindMainActivityViewModel(mainActivityViewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainFragmentViewModel::class)
    fun bindMainFragmentViewModel(mainFragmentViewModel: MainFragmentViewModel): ViewModel

    @Binds
    fun bindUpnpManager(defaultUpnpManager: DefaultUpnpManager): UpnpManager

    @ContributesAndroidInjector
    fun contributeMainFragment(): MainFragment

    @ContributesAndroidInjector
    fun contritubeSettingsFragment(): SettingsFragment
}