package com.m3sv.plainupnp.presentation.settings

import dagger.Subcomponent

@SettingsScope
@Subcomponent
interface SettingsComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): SettingsComponent
    }

    fun inject(settingsFragment: SettingsFragment)
}
