package com.m3sv.plainupnp.di.activity

import com.m3sv.plainupnp.di.UPnPBinder
import com.m3sv.plainupnp.presentation.tv.TvActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface TvActivityBuilder {
    @ContributesAndroidInjector(
            modules = [
                TvActivityModule::class]
    )
    fun contributeTvActivity(): TvActivity
}