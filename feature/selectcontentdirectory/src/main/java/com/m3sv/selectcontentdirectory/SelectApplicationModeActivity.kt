package com.m3sv.selectcontentdirectory

import android.content.Intent
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
                        onNextClick = ::onFinish,
                        onBackClick = ::onFinish,
                        nextText = getString(R.string.done),
                        initialMode = applicationModeManager.getApplicationMode()
                    ) { applicationMode ->
                        lifecycleScope.launch { applicationModeManager.setApplicationMode(applicationMode) }
                    }
                }
            }
        }
    }

    private fun onFinish() {
        startActivity(Intent(applicationContext, SelectContentDirectoryActivity::class.java).apply {
            flags += Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
