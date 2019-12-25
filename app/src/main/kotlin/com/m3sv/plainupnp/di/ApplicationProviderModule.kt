package com.m3sv.plainupnp.di

import android.app.Application
import com.m3sv.plainupnp.App
import dagger.Module
import dagger.Provides

@Module
class ApplicationProviderModule(private val app: App) {

    @Provides
    fun provideApplication(): Application = app
}
