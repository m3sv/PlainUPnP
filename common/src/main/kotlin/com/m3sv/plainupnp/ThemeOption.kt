package com.m3sv.plainupnp

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.m3sv.plainupnp.common.R

enum class ThemeOption(@StringRes val text: Int) {
    System(text = R.string.system_theme_label),
    Light(text = R.string.light_theme_label),
    Dark(text = R.string.dark_theme_label);

    @Composable
    fun isDarkTheme(): Boolean = when (this) {
        System -> isSystemInDarkTheme()
        else -> this == Dark
    }
}
