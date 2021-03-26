package com.m3sv.plainupnp.presentation.onboarding

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.m3sv.plainupnp.ThemeOption
import com.m3sv.plainupnp.applicationmode.ApplicationMode
import com.m3sv.plainupnp.applicationmode.SelectApplicationModeScreen
import com.m3sv.plainupnp.compose.util.AppTheme
import com.m3sv.plainupnp.data.upnp.UriWrapper
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    @Inject
    lateinit var onboardingManager: OnboardingManager

    private var onPermissionResult by mutableStateOf(-1)

    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LayoutContainer() }
    }

    override fun onBackPressed() {
        viewModel.onNavigateBack()
    }

    @Composable
    private fun LayoutContainer() {
        when (onPermissionResult) {
            0 -> {
                viewModel.onNavigateNext()
                onPermissionResult = -1
            }
            1 -> {
                // TODO handle permission denied
                onPermissionResult = -1
            }
            else -> Unit
        }

        val contentUris by viewModel.contentUris.collectAsState()
        val currentScreen by viewModel.currentScreen.collectAsState()

        Content(
            selectedTheme = viewModel.activeTheme,
            currentScreen = currentScreen,
            stringProvider = this@OnboardingActivity::getString,
            onSelectTheme = viewModel::onSelectTheme,
            onSelectApplicationMode = viewModel::onSelectMode,
            contentUris = contentUris,
            onNext = viewModel::onNavigateNext,
            onBack = viewModel::onNavigateBack
        )
    }

    @Composable
    private fun Content(
        selectedTheme: ThemeOption,
        currentScreen: OnboardingScreen,
        contentUris: List<UriWrapper> = listOf(),
        stringProvider: (Int) -> String,
        onSelectTheme: (ThemeOption) -> Unit,
        onSelectApplicationMode: (ApplicationMode) -> Unit,
        onNext: () -> Unit,
        onBack: () -> Unit,
    ) {
        AppTheme {
            Surface {
                when (currentScreen) {
                    OnboardingScreen.Greeting -> GreetingScreen(onNext)
                    OnboardingScreen.StoragePermission -> StoragePermissionScreen(onBackClick = onBack) {
                        checkStoragePermission(onNext)
                    }
                    OnboardingScreen.SelectDirectories -> SelectFoldersScreen(
                        contentUris,
                        selectDirectory = ::openDirectory,
                        onBackClick = onBack,
                        onNext = onNext,
                        onReleaseUri = viewModel::releaseUri,
                    )
                    OnboardingScreen.SelectTheme -> SelectThemeScreen(
                        titleText = getString(R.string.set_theme_label),
                        selectedTheme = selectedTheme,
                        stringProvider = { stringProvider(it.text) },
                        onThemeOptionSelected = onSelectTheme,
                        onClick = onNext,
                        onBackClick = onBack
                    )
                    OnboardingScreen.SelectMode -> SelectApplicationModeScreen(
                        onNextClick = onNext,
                        onBackClick = onBack,
                        stringProvider = stringProvider,
                        onItemSelected = onSelectApplicationMode
                    )
                    OnboardingScreen.Finish -> onboardingManager.completeOnboarding(this@OnboardingActivity)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                onPermissionResult =
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        0
                    } else {
                        1
                    }
            }
        }
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

    private fun checkStoragePermission(onPermissionGranted: () -> Unit) {
        when {
            viewModel.hasStoragePermission() -> onPermissionGranted()
            ActivityCompat.shouldShowRequestPermissionRationale(this, OnboardingViewModel.STORAGE_PERMISSION)
            -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                // TODO explain why we need storage permission
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName"))
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            else -> {
                requestReadStoragePermission()
            }
        }
    }

    private fun requestReadStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!viewModel.hasStoragePermission()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(OnboardingViewModel.STORAGE_PERMISSION),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    @Composable
    @Preview
    private fun PreviewContent() {
        Content(
            selectedTheme = ThemeOption.Light,
            currentScreen = OnboardingScreen.SelectTheme,
            stringProvider = { "" },
            onSelectTheme = {},
            onSelectApplicationMode = {},
            onNext = {},
            onBack = {}
        )
    }

    companion object {
        private const val REQUEST_READ_EXTERNAL_STORAGE = 12345
        private const val REQUEST_DIRECTORY_CODE = 12
        private const val DIRECTORY_PERMISSIONS =
            Intent.FLAG_GRANT_READ_URI_PERMISSION and FLAG_GRANT_PERSISTABLE_URI_PERMISSION
    }
}
