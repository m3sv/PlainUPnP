package com.m3sv.plainupnp.di.activity

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.di.ViewModelKey
import com.m3sv.plainupnp.di.scope.ActivityScope
import com.m3sv.plainupnp.presentation.main.MainActivityViewModel
import com.m3sv.plainupnp.presentation.upnp.UpnpFragment
import com.m3sv.plainupnp.presentation.upnp.UpnpViewModel
import com.m3sv.plainupnp.presentation.settings.SettingsFragment
import com.m3sv.plainupnp.presentation.tv.TvActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
interface TvActivityModule {
    @Binds
    fun provideAppCompatActivity(mainActivity: TvActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    fun bindMainActivityViewModel(mainActivityViewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UpnpViewModel::class)
    fun bindMainFragmentViewModel(upnpViewModel: UpnpViewModel): ViewModel

    @ActivityScope
    @ContributesAndroidInjector
    fun contributeMainFragment(): UpnpFragment

    @ActivityScope
    @ContributesAndroidInjector
    fun contritubeSettingsFragment(): SettingsFragment
}