package com.m3sv.plainupnp.di

import android.content.Context
import com.m3sv.plainupnp.App
import com.m3sv.plainupnp.presentation.home.HomeComponent
import com.m3sv.plainupnp.presentation.main.di.MainActivitySubComponent
import com.m3sv.plainupnp.presentation.onboarding.ConfigureFolderActivity
import com.m3sv.plainupnp.presentation.onboarding.OnboardingActivity
import com.m3sv.plainupnp.presentation.settings.SettingsComponent
import com.m3sv.plainupnp.presentation.splash.SplashActivity
import com.m3sv.plainupnp.upnp.di.UpnpBindersModule
import com.m3sv.plainupnp.upnp.di.UpnpSubComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        UpnpBindersModule::class,
        BinderModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun mainSubComponent(): MainActivitySubComponent.Factory
    fun homeSubComponent(): HomeComponent.Factory
    fun settingsSubComponent(): SettingsComponent.Factory
    fun upnpSubComponent(): UpnpSubComponent.Factory

    fun inject(app: App)
    fun inject(splashActivity: SplashActivity)
    fun inject(onboardingActivity: OnboardingActivity)
    fun inject(configureFolderActivity: ConfigureFolderActivity)
}
