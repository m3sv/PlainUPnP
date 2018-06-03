package com.m3sv.droidupnp.di.activity

import android.arch.lifecycle.ViewModel
import android.support.v7.app.AppCompatActivity
import com.m3sv.droidupnp.di.ViewModelKey
import com.m3sv.droidupnp.presentation.main.MainActivityViewModel
import com.m3sv.droidupnp.presentation.main.MainActivity
import com.m3sv.droidupnp.presentation.main.MainFragment
import com.m3sv.droidupnp.presentation.settings.SettingsFragment
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

    @ContributesAndroidInjector
    fun contributeMainFragment(): MainFragment

    @ContributesAndroidInjector
    fun contritubeSettingsFragment(): SettingsFragment
}