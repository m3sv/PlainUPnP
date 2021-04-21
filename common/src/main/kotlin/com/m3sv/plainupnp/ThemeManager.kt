package com.m3sv.plainupnp

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.m3sv.plainupnp.common.R
import com.m3sv.plainupnp.common.preferences.PreferencesRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    private val application: Application,
    private val sharedPreferences: SharedPreferences,
    private val preferencesRepository: PreferencesRepository,
) {
    private val setThemeKey = application.getString(R.string.key_set_theme)

    private val scope = MainScope()

    val currentTheme: ThemeOption
        get() {
            val currentTheme = requireNotNull(sharedPreferences.getString(
                setThemeKey,
                application.getString(ThemeOption.System.text))
            )

            return ThemeOption.fromString(application, currentTheme) ?: ThemeOption.System
        }

    fun setDefaultNightMode() {
        AppCompatDelegate.setDefaultNightMode(currentTheme.mode)
    }

    fun setNightMode(mode: ThemeOption) {
        scope.launch { preferencesRepository.setApplicationTheme(mode) }
        sharedPreferences.edit().putString(setThemeKey, application.getString(mode.text)).apply()
        setDefaultNightMode()
    }
}
