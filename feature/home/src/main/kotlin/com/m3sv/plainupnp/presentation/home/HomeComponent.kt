package com.m3sv.plainupnp.presentation.home

import dagger.Subcomponent

@HomeScope
@Subcomponent(
    modules = [HomeFragmentModule::class]
)
interface HomeComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): HomeComponent
    }

    fun inject(homeFragment: HomeFragment)
}
