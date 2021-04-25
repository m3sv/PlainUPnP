package com.m3sv.plainupnp.presentation.onboarding.selecttheme

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.ui.res.stringResource
import com.m3sv.plainupnp.compose.util.AppTheme
import com.m3sv.plainupnp.presentation.onboarding.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectThemeActivity : AppCompatActivity() {

    private val viewModel: SelectThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {
                    SelectThemeScreen(
                        titleText = stringResource(R.string.set_theme_label),
                        buttonText = stringResource(id = R.string.done),
                        selectedTheme = viewModel.activeTheme,
                        onThemeOptionSelected = viewModel::onSelectTheme,
                        onClick = { finish() },
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}
