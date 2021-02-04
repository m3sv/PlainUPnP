package com.m3sv.plainupnp.presentation.home

import dagger.Subcomponent

@HomeScope
@Subcomponent
interface HomeComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): HomeComponent
    }

    fun inject(homeFragment: HomeFragment)
}
