package com.m3sv.plainupnp.common.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.m3sv.plainupnp.common.R


fun Context.updateTheme() {
    val setThemeKey = getString(R.string.set_theme_key)
    val lightThemeKey = getString(R.string.light_theme_value)
    val darkThemeKey = getString(R.string.dark_theme_value)
    val systemBasedThemeKey = getString(R.string.system_theme_value)
    val batterySaverThemeKey = getString(R.string.battery_saver_theme_value)

    val value = PreferenceManager
        .getDefaultSharedPreferences(this)
        .getString(
            setThemeKey,
            systemBasedThemeKey
        )

    val defaultNightMode = when (value) {
        darkThemeKey -> AppCompatDelegate.MODE_NIGHT_YES
        lightThemeKey -> AppCompatDelegate.MODE_NIGHT_NO
        batterySaverThemeKey -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    AppCompatDelegate.setDefaultNightMode(defaultNightMode)
}
