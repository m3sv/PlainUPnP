package com.m3sv.plainupnp.presentation.onboarding

import android.app.Application
import android.content.UriPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.m3sv.plainupnp.ThemeManager
import com.m3sv.plainupnp.ThemeOption

class OnboardingViewModel(application: Application, private val themeManager: ThemeManager) :
    AndroidViewModel(application) {

    var activeTheme: ThemeOption by mutableStateOf(themeManager.currentTheme)
        private set

    var currentScreen: OnboardingScreen by mutableStateOf(OnboardingScreen.Greeting)
        private set

    var contentUris: List<UriPermission> by mutableStateOf(application.contentResolver.persistedUriPermissions)
        private set

    fun onThemeChange(themeOption: ThemeOption) {
        activeTheme = themeOption
        themeManager.setNightMode(themeOption)
    }

    fun onPageChange(onboardingScreen: OnboardingScreen) {
        currentScreen = onboardingScreen
    }

    fun saveUri() {
        contentUris = getApplication<Application>().contentResolver.persistedUriPermissions
    }
}
