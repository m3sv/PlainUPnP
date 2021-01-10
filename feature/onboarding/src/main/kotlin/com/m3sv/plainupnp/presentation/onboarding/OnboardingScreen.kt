package com.m3sv.plainupnp.presentation.onboarding

enum class OnboardingScreen {
    Greeting,
    StoragePermission,
    SelectDirectories,
    SelectTheme;

    val next: OnboardingScreen
        get() = when (this.ordinal) {
            values().size - 1 -> this
            else -> values()[this.ordinal + 1]
        }

    val previous: OnboardingScreen
        get() = when (this.ordinal) {
            0 -> Greeting
            else -> values()[this.ordinal - 1]
        }
}
