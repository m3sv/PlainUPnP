package com.m3sv.plainupnp.common.util

import android.app.Application
import androidx.annotation.StringRes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

fun interface StringResolver {
    fun getString(@StringRes id: Int): String?
}

@Module
@InstallIn(SingletonComponent::class)
object StringResolverModule {

    @Provides
    fun provideStringResolver(application: Application): StringResolver = StringResolver(application::getString)
}
