package com.m3sv.plainupnp.di

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.m3sv.plainupnp.core.persistence.Database
import com.m3sv.plainupnp.presentation.main.MainActivity
import com.m3sv.plainupnp.presentation.onboarding.OnboardingManager
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
object AppModule {

    @Provides
    @JvmStatic
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @JvmStatic
    @Singleton
    fun provideOnboardingManager(preferences: SharedPreferences): OnboardingManager =
        OnboardingManager(preferences) { activity ->
            activity.startActivity(Intent(activity, MainActivity::class.java))
        }

    @Provides
    @JvmStatic
    @Singleton
    fun provideDatabase(context: Context) =
        Database(AndroidSqliteDriver(Database.Schema, context, "plainupnp.db"))

}
