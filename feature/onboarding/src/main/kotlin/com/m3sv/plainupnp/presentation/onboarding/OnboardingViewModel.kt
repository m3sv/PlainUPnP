package com.m3sv.plainupnp.presentation.onboarding

import android.app.Application
import android.content.UriPermission
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    var activeTheme: ThemeOption by mutableStateOf(ThemeOption.Light)
        private set

    var currentScreen: OnboardingScreen by mutableStateOf(OnboardingScreen.Greeting)
        private set

    var contentUris: List<UriPermission> by mutableStateOf(application.contentResolver.persistedUriPermissions)
        private set

    fun onThemeChange(themeOption: ThemeOption) {
        activeTheme = themeOption
        AppCompatDelegate.setDefaultNightMode(themeOption.mode)
    }

    fun onPageChange(onboardingScreen: OnboardingScreen) {
        currentScreen = onboardingScreen
    }

    fun saveUri() {
        contentUris = getApplication<Application>().contentResolver.persistedUriPermissions
    }
}
