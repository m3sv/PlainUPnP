package com.m3sv.droidupnp.di.activity

import com.m3sv.droidupnp.presentation.settings.SettingsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
interface SettingsActivityBuilder {
    @ContributesAndroidInjector(
        modules = [
            SettingsActivityModule::class]
    )
    fun contributeSettingsActivity(): SettingsActivity
}