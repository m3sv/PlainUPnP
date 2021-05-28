package com.m3sv.plainupnp.presentation.onboarding.selecttheme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.m3sv.plainupnp.ThemeManager
import com.m3sv.plainupnp.compose.util.AppTheme
import com.m3sv.plainupnp.presentation.onboarding.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectThemeActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val activeTheme by themeManager.collectTheme()

            AppTheme(activeTheme) {
                Surface {
                    SelectThemeScreen(
                        titleText = stringResource(R.string.set_theme_label),
                        buttonText = stringResource(id = R.string.done),
                        selectedTheme = activeTheme,
                        onThemeOptionSelected = themeManager::setTheme,
                        onClick = { finish() },
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}
