package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.di.AppComponent
import dagger.Component

@HomeScope
@Component(
    dependencies = [AppComponent::class],
    modules = [HomeFragmentModule::class]
)
interface HomeComponent {

    @Component.Factory
    interface Factory {
        fun create(appComponent: AppComponent): HomeComponent
    }

    fun inject(homeFragment: HomeFragment)
}
