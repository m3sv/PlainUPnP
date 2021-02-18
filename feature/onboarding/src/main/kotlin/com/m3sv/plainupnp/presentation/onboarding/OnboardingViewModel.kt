package com.m3sv.plainupnp.presentation.onboarding

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.m3sv.plainupnp.ContentManager
import com.m3sv.plainupnp.ThemeManager
import com.m3sv.plainupnp.ThemeOption
import com.m3sv.plainupnp.data.upnp.UriWrapper
import kotlinx.coroutines.flow.Flow

class OnboardingViewModel(
    application: Application,
    private val themeManager: ThemeManager,
    private val contentManager: ContentManager,
) :
    AndroidViewModel(application) {

    var activeTheme: ThemeOption by mutableStateOf(themeManager.currentTheme)
        private set

    var currentScreen: OnboardingScreen by mutableStateOf(OnboardingScreen.Greeting)
        private set

    val contentUris: Flow<List<UriWrapper>> = contentManager.getPersistedUris()

    fun onThemeChange(themeOption: ThemeOption) {
        activeTheme = themeOption
        themeManager.setNightMode(themeOption)
    }

    fun onPageChange(onboardingScreen: OnboardingScreen) {
        currentScreen = onboardingScreen
    }

    fun saveUri() {
        contentManager.updateUris()
    }

    fun releaseUri(uriWrapper: UriWrapper) {
        contentManager.releaseUri(uriWrapper)
    }
}
