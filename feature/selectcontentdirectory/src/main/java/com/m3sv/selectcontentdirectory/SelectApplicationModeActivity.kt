package com.m3sv.selectcontentdirectory

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import com.m3sv.plainupnp.applicationmode.SelectApplicationModeScreen
import com.m3sv.plainupnp.compose.util.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectApplicationModeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {
                    SelectApplicationModeScreen(
                        onNextClick = { finish() },
                        onBackClick = { finish() },
                        nextText = getString(R.string.done),
                        stringProvider = { getString(it) }) {

                    }
                }
            }
        }
    }
}
