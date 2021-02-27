package com.m3sv.plainupnp

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.m3sv.plainupnp.common.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    private val application: Application,
    private val sharedPreferences: SharedPreferences,
) {
    private val setThemeKey = application.getString(R.string.set_theme_key)

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
        sharedPreferences.edit().putString(setThemeKey, application.getString(mode.text)).apply()
        setDefaultNightMode()
    }
}
