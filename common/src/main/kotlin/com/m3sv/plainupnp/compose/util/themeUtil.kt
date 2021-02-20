package com.m3sv.plainupnp.compose.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.m3sv.plainupnp.common.R

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = if (isSystemInDarkTheme())
        darkColors(primary = colorResource(id = R.color.colorPrimary))
    else lightColors(
        primary = colorResource(id = R.color.colorPrimary)), content = content)
}
