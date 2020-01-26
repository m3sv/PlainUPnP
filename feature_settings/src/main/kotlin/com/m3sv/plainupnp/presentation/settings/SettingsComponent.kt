package com.m3sv.plainupnp.presentation.settings

import com.m3sv.plainupnp.di.AppComponent
import dagger.Component

@SettingsScope
@Component(dependencies = [AppComponent::class])
interface SettingsComponent {

    @Component.Factory
    interface Factory {
        fun create(appComponent: AppComponent): SettingsComponent
    }

    fun inject(homeFragment: SettingsFragment)
}
