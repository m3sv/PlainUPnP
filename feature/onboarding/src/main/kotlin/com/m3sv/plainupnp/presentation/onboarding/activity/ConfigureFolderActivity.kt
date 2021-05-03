package com.m3sv.plainupnp.presentation.onboarding.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.m3sv.plainupnp.ThemeManager
import com.m3sv.plainupnp.compose.util.AppTheme
import com.m3sv.plainupnp.data.upnp.UriWrapper
import com.m3sv.plainupnp.presentation.onboarding.OnboardingViewModel
import com.m3sv.plainupnp.presentation.onboarding.SelectFoldersScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConfigureFolderActivity : AppCompatActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val contentUris: List<UriWrapper> by viewModel.contentUris.collectAsState()

            AppTheme {
                Surface {
                    SelectFoldersScreen(
                        contentUris = contentUris,
                        selectDirectory = { openDirectory() },
                        onReleaseUri = { viewModel.releaseUri(it) },
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }

    private fun openDirectory() {
        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            // Provide read access to files and sub-directories in the user-selected
            // directory.
            flags = DIRECTORY_PERMISSIONS
        }

        startActivityForResult(intent, REQUEST_DIRECTORY_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_DIRECTORY_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.also { uri ->
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        viewModel.saveUri()
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_DIRECTORY_CODE = 12
        private const val DIRECTORY_PERMISSIONS =
            Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
    }
}
