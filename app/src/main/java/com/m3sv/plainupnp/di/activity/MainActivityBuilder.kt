package com.m3sv.plainupnp.di.activity

import com.m3sv.plainupnp.presentation.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
interface MainActivityBuilder {
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    fun contributeMainActivity(): MainActivity
}