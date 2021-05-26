package com.m3sv.plainupnp.presentation.home

import android.content.Context
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.fetch.VideoFrameFileFetcher
import coil.fetch.VideoFrameUriFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    fun provideImageLoader(@ApplicationContext context: Context) = ImageLoader.Builder(context)
        .componentRegistry {
            add(VideoFrameFileFetcher(context))
            add(VideoFrameUriFetcher(context))
            add(VideoFrameDecoder(context))
        }
        .build()
}
