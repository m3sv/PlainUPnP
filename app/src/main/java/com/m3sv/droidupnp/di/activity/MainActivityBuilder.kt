package com.m3sv.droidupnp.di.activity

import com.m3sv.droidupnp.presentation.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
interface MainActivityBuilder {
    @ContributesAndroidInjector(
        modules = [
            MainActivityModule::class]
    )
    fun contributeMainActivity(): MainActivity
}