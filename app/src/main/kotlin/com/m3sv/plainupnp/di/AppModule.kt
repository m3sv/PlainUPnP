package com.m3sv.plainupnp.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
object
AppModule {
    @Provides
    @JvmStatic
    @Singleton
    fun provideContext(app: Application): Context = app.applicationContext

    @Provides
    @JvmStatic
    @Singleton
    fun provideSharedPreferences(app: Application): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app)
}
