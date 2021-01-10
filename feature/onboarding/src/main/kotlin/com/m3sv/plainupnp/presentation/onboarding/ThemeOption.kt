package com.m3sv.plainupnp.presentation.onboarding

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate.*

enum class ThemeOption(val mode: Int, @StringRes val text: Int) {
    Light(mode = MODE_NIGHT_NO, text = R.string.light_theme_label),
    Dark(mode = MODE_NIGHT_YES, text = R.string.dark_theme_label),
    System(mode = MODE_NIGHT_FOLLOW_SYSTEM, text = R.string.system_theme_label)
}
