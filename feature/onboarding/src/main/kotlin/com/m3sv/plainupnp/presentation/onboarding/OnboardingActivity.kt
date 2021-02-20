package com.m3sv.plainupnp.presentation.onboarding

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.m3sv.plainupnp.ContentManager
import com.m3sv.plainupnp.ThemeManager
import com.m3sv.plainupnp.ThemeOption
import com.m3sv.plainupnp.compose.util.AppTheme
import com.m3sv.plainupnp.data.upnp.UriWrapper
import java.util.*
import javax.inject.Inject

class OnboardingActivity : AppCompatActivity() {

    @Inject
    lateinit var onboardingManager: OnboardingManager

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var contentManager: ContentManager

    private var onPermissionResult by mutableStateOf(-1)

    private val viewModel: OnboardingViewModel by viewModels(factoryProducer = {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return OnboardingViewModel(application, themeManager, contentManager) as T
            }
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as OnboardingInjector).inject(this)
        setContent { LayoutContainer() }
    }

    override fun onBackPressed() {
        viewModel.onPageChange(viewModel.currentScreen.previous)
    }

    @Composable
    private fun LayoutContainer() {
        when (onPermissionResult) {
            0 -> {
                viewModel.onPageChange(OnboardingScreen.SelectDirectories)
                onPermissionResult = -1
            }
            1 -> {
                // TODO handle permission denied
                onPermissionResult = -1
            }
            else -> Unit
        }

        val contentUris by viewModel.contentUris.collectAsState(initial = listOf())

        Content(
            selectedTheme = viewModel.activeTheme,
            currentScreen = viewModel.currentScreen,
            onPageChange = viewModel::onPageChange,
            stringProvider = this@OnboardingActivity::getString,
            onThemeOptionSelected = viewModel::onThemeChange,
            contentUris = contentUris
        )
    }

    @Composable
    private fun Content(
        selectedTheme: ThemeOption,
        currentScreen: OnboardingScreen,
        contentUris: List<UriWrapper> = listOf(),
        stringProvider: (Int) -> String,
        onThemeOptionSelected: (ThemeOption) -> Unit,
        onPageChange: (OnboardingScreen) -> Unit,
    ) {
        AppTheme {
            Surface {
                when (currentScreen) {
                    OnboardingScreen.Greeting -> GreetingScreen { onPageChange(currentScreen.next) }
                    OnboardingScreen.StoragePermission -> StoragePermissionScreen(onBackClick = {
                        onPageChange(currentScreen.previous)
                    }) {
                        checkStoragePermission { onPageChange(currentScreen.next) }
                    }
                    OnboardingScreen.SelectDirectories -> SelectFoldersScreen(
                        contentUris,
                        selectDirectory = { openDirectory() },
                        onBackClick = { onPageChange(currentScreen.previous) },
                        onNext = { onboardingManager.completeOnboarding(this@OnboardingActivity) },
                        onReleaseUri = { viewModel.releaseUri(it) },
                    )
                    OnboardingScreen.SelectTheme -> SelectThemeScreen(
                        text = getString(R.string.set_theme_label),
                        selectedTheme = selectedTheme,
                        themeOptions = ThemeOption.values().toList(),
                        stringProvider = stringProvider,
                        onThemeOptionSelected = onThemeOptionSelected,
                        onClick = { onPageChange(currentScreen.next) },
                        onBackClick = { onPageChange(currentScreen.previous) })
                }
            }

//            Box(modifier = Modifier.fillMaxSize()) {
//                Row(modifier = Modifier.align(Alignment.BottomCenter),
//                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                    for (i in OnboardingScreen.values().indices) {
//                        Box(modifier = Modifier
//                            .size(8.dp)
//                            .clip(CircleShape)
//                            .background(animate(if (currentScreen.ordinal == i) Color.Red else Color.Yellow))
//                        )
//                    }
//                }
//            }
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
            ContextCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> onPermissionGranted()
            ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)
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
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_EXTERNAL_STORAGE),
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
            onPageChange = { },
            stringProvider = { "" },
            onThemeOptionSelected = {}
        )
    }

    companion object {
        private const val REQUEST_READ_EXTERNAL_STORAGE = 12345
        private const val REQUEST_DIRECTORY_CODE = 12
        private const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val DIRECTORY_PERMISSIONS =
            Intent.FLAG_GRANT_READ_URI_PERMISSION and FLAG_GRANT_PERSISTABLE_URI_PERMISSION
    }
}
