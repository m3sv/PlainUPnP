package com.m3sv.droidupnp.di.activity

import android.arch.lifecycle.ViewModel
import android.support.v7.app.AppCompatActivity
import com.m3sv.droidupnp.di.ViewModelKey
import com.m3sv.droidupnp.presentation.settings.SettingsActivity
import com.m3sv.droidupnp.presentation.settings.SettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
interface SettingsActivityModule {
    @Binds
    fun provideSettingsActivity(mainActivity: SettingsActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel
}