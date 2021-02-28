package com.m3sv.selectcontentdirectory

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.material.Text
import com.m3sv.plainupnp.compose.util.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectContentDirectoryActivity : AppCompatActivity() {

    private val viewModel: SelectContentDirectoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {
                    Text("Hello world")
                }
            }
        }
    }
}
