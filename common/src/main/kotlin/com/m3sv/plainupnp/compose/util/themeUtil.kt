package com.m3sv.plainupnp.compose.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.m3sv.plainupnp.ThemeOption
import com.m3sv.plainupnp.common.R

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val primaryColor = colorResource(id = R.color.colorPrimary)

    val colors = when (isSystemInDarkTheme()) {
        false -> lightColors(
            primary = primaryColor,
            secondary = primaryColor,
            secondaryVariant = primaryColor
        )
        else -> darkColors(
            primary = primaryColor,
            secondary = primaryColor,
            secondaryVariant = primaryColor
        )
    }

    MaterialTheme(colors, content = content)
}

@Composable
fun AppTheme(themeOption: ThemeOption, content: @Composable () -> Unit) {
    val primaryColor = colorResource(id = R.color.colorPrimary)

    val colors = when (themeOption) {
        ThemeOption.Light, ThemeOption.System -> lightColors(primary = primaryColor)
        ThemeOption.Dark -> darkColors(primary = primaryColor)
    }

    MaterialTheme(colors, content = content)
}
