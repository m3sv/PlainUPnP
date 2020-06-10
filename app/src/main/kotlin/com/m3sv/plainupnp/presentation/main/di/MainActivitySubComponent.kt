package com.m3sv.plainupnp.presentation.main.di

import com.m3sv.plainupnp.presentation.main.ControlsFragment
import com.m3sv.plainupnp.presentation.main.MainActivity
import dagger.Subcomponent

@Subcomponent(modules = [MainActivityModule::class])
interface MainActivitySubComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainActivitySubComponent
    }

    fun inject(activity: MainActivity)

    fun inject(fragment: ControlsFragment)
}
