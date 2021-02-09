package com.m3sv.plainupnp.presentation.onboarding


interface OnboardingInjector {
    fun inject(onboardingActivity: OnboardingActivity)
    fun inject(configureFolderActivity: ConfigureFolderActivity)
}
