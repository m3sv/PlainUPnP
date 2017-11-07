package com.m3sv.droidupnp.di

import com.m3sv.presentation.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class BuildersModule {

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity
}