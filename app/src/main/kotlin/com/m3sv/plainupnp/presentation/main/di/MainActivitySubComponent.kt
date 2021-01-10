package com.m3sv.plainupnp.presentation.main.di

import com.m3sv.plainupnp.presentation.main.ComposeActivity
import com.m3sv.plainupnp.presentation.main.MainActivity
import com.m3sv.plainupnp.presentation.main.controls.ControlsFragment
import dagger.Subcomponent

@Subcomponent
interface MainActivitySubComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainActivitySubComponent
    }

    fun inject(activity: MainActivity)

    fun inject(activity: ComposeActivity)

    fun inject(fragment: ControlsFragment)
}
