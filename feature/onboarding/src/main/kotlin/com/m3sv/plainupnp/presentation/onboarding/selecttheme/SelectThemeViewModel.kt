package com.m3sv.plainupnp.presentation.onboarding.selecttheme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.ThemeManager
import com.m3sv.plainupnp.ThemeOption
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectThemeViewModel @Inject constructor(private val themeManager: ThemeManager) : ViewModel() {
    var activeTheme: ThemeOption by mutableStateOf(themeManager.currentTheme)
        private set

    fun onSelectTheme(themeOption: ThemeOption) {
        activeTheme = themeOption
        themeManager.setNightMode(themeOption)
    }
}
