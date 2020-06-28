package com.m3sv.plainupnp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.m3sv.plainupnp.core.persistence.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

private const val DATABASE_NAME = "plainupnp.db"

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
    fun provideSqlDriver(context: Context): SqlDriver =
        AndroidSqliteDriver(Database.Schema, context, DATABASE_NAME)

    @Provides
    @JvmStatic
    @Singleton
    fun provideDatabase(sqlDriver: SqlDriver) = Database(sqlDriver)

}
