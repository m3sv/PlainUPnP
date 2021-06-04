package com.m3sv.plainupnp.di

import android.app.Application
import android.content.Intent
import com.m3sv.plainupnp.common.preferences.PreferencesRepository
import com.m3sv.plainupnp.core.persistence.Database
import com.m3sv.plainupnp.presentation.onboarding.OnboardingManager
import com.m3sv.selectcontentdirectory.SelectContentDirectoryActivity
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOnboardingManager(preferencesRepository: PreferencesRepository): OnboardingManager =
        OnboardingManager(preferencesRepository = preferencesRepository) { activity ->
            activity.startActivity(Intent(activity, SelectContentDirectoryActivity::class.java))
        }

    @Provides
    @Singleton
    fun provideDatabase(application: Application) =
        Database(AndroidSqliteDriver(Database.Schema, application, "plainupnp.db"))
}
