package com.m3sv.plainupnp.di.activity

import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.di.ViewModelKey
import com.m3sv.plainupnp.di.scope.ActivityScope
import com.m3sv.plainupnp.presentation.main.MainActivityViewModel
import com.m3sv.plainupnp.presentation.content.ContentFragment
import com.m3sv.plainupnp.presentation.content.ContentViewModel
import com.m3sv.plainupnp.presentation.settings.SettingsFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
interface MainActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    fun bindMainActivityViewModel(mainActivityViewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContentViewModel::class)
    fun bindMainFragmentViewModel(contentViewModel: ContentViewModel): ViewModel

    @ActivityScope
    @ContributesAndroidInjector
    fun contributeMainFragment(): ContentFragment

    @ActivityScope
    @ContributesAndroidInjector
    fun contritubeSettingsFragment(): SettingsFragment
}