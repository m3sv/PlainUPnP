package com.m3sv.droidupnp.di

import android.arch.lifecycle.ViewModel
import android.support.v7.app.AppCompatActivity
import com.m3sv.droidupnp.presentation.main.MainActivityViewModel
import com.m3sv.presentation.main.MainActivity
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
interface MainActivityModule {
    @Binds
    fun provideAppCompatActivity(mainActivity: MainActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    fun bindMainActivityViewModel(mainActivityViewModel: MainActivityViewModel): ViewModel
}