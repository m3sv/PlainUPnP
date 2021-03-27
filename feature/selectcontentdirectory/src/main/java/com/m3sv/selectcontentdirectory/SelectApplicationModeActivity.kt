package com.m3sv.selectcontentdirectory

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.lifecycle.lifecycleScope
import com.m3sv.plainupnp.applicationmode.ApplicationModeManager
import com.m3sv.plainupnp.applicationmode.SelectApplicationModeScreen
import com.m3sv.plainupnp.compose.util.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectApplicationModeActivity : AppCompatActivity() {

    @Inject
    lateinit var applicationModeManager: ApplicationModeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {
                    SelectApplicationModeScreen(
                        onNextClick = { finish() },
                        onBackClick = { finish() },
                        nextText = getString(R.string.done),
                        stringProvider = { getString(it) },
                        initialMode = applicationModeManager.getApplicationMode()
                    ) { applicationMode ->
                        lifecycleScope.launch { applicationModeManager.setApplicationMode(applicationMode) }
                    }
                }
            }
        }
    }
}
