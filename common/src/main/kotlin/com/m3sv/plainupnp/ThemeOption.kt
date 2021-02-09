package com.m3sv.plainupnp

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate.*
import com.m3sv.plainupnp.common.R

enum class ThemeOption(val mode: Int, @StringRes val text: Int) {
    System(mode = MODE_NIGHT_FOLLOW_SYSTEM, text = R.string.system_theme_label),
    Light(mode = MODE_NIGHT_NO, text = R.string.light_theme_label),
    Dark(mode = MODE_NIGHT_YES, text = R.string.dark_theme_label);

    companion object {
        fun fromString(context: Context, currentTheme: String): ThemeOption? = values()
            .firstOrNull { context.getString(it.text) == currentTheme }
    }
}
