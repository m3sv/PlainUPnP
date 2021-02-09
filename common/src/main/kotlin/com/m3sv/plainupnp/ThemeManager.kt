package com.m3sv.plainupnp

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.m3sv.plainupnp.common.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
) {
    private val setThemeKey = context.getString(R.string.set_theme_key)

    val currentTheme: ThemeOption
        get() {
            val currentTheme = requireNotNull(sharedPreferences.getString(
                setThemeKey,
                context.getString(ThemeOption.System.text))
            )

            return ThemeOption.fromString(context, currentTheme) ?: ThemeOption.System
        }

    fun setDefaultNightMode() {
        AppCompatDelegate.setDefaultNightMode(currentTheme.mode)
    }

    fun setNightMode(mode: ThemeOption) {
        sharedPreferences.edit().putString(setThemeKey, context.getString(mode.text)).apply()
        setDefaultNightMode()
    }
}
