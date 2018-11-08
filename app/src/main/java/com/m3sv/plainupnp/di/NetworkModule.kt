package com.m3sv.plainupnp.di

import com.m3sv.plainupnp.di.scope.ApplicationScope
import com.m3sv.plainupnp.network.ApiManager
import com.m3sv.plainupnp.network.BuildConfig
import com.m3sv.plainupnp.network.DefaultApiManager
import com.m3sv.plainupnp.network.TasteDiveService
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

@Module
object NetworkModule {

    @ApplicationScope
    @Provides
    @JvmStatic
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { log ->
            Timber.tag("REST_LOGGER").d(log)
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @ApplicationScope
    @JvmStatic
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .retryOnConnectionFailure(false)
            .addInterceptor(httpLoggingInterceptor)
            .build()

    @Provides
    @ApplicationScope
    @JvmStatic
    fun provideTasteDiveRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.TASTE_DIVE_BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

    @Provides
    @ApplicationScope
    @JvmStatic
    fun provideTaseDiveService(retrofit: Retrofit): TasteDiveService =
        retrofit.create(TasteDiveService::class.java)
}
