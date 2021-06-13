package com.m3sv.selectcontentdirectory

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.m3sv.plainupnp.Router
import com.m3sv.plainupnp.ThemeManager
import com.m3sv.plainupnp.common.util.pass
import com.m3sv.plainupnp.compose.util.AppTheme
import com.m3sv.plainupnp.compose.widgets.OnePane
import com.m3sv.plainupnp.compose.widgets.OneTitle
import com.m3sv.plainupnp.compose.widgets.OneToolbar
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.upnp.manager.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SelectContentDirectoryActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    private val viewModel by viewModels<SelectContentDirectoryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUpnpService()

        setContent {
            val contentDirectories by viewModel.state.collectAsState()
            val currentTheme by themeManager.collectTheme()
            var loadingDeviceDisplay: DeviceDisplay? by remember { mutableStateOf(null) }

            fun DeviceDisplay.isLoading(): Boolean = loadingDeviceDisplay != null && loadingDeviceDisplay == this

            AppTheme(currentTheme.isDarkTheme()) {
                Surface {
                    OnePane(viewingContent = {
                        OneTitle(text = "Select content directory")
                        OneToolbar {
                            Image(
                                modifier = Modifier
                                    .clickable { handleGearClick() }
                                    .padding(8.dp),
                                painter = painterResource(id = R.drawable.ic_settings),
                                contentDescription = null
                            )
                        }
                    }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            if (contentDirectories.isEmpty())
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 24.dp,
                                        vertical = 24.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        getString(R.string.content_directory_search_message),
                                        Modifier.weight(1f),
                                        style = MaterialTheme.typography.body1
                                    )
                                    CircularProgressIndicator(Modifier.size(32.dp))
                                }
                            else
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    content = {
                                        itemsIndexed(contentDirectories) { index, item ->
                                            Column(modifier = Modifier.clickable(enabled = loadingDeviceDisplay == null) {
                                                loadingDeviceDisplay = item

                                                lifecycleScope.launch(Dispatchers.IO) {
                                                    when (viewModel
                                                        .selectContentDirectoryAsync(item.upnpDevice)
                                                        .await()
                                                    ) {
                                                        Result.Error -> withContext(Dispatchers.Main) { handleSelectDirectoryError() }
                                                        Result.Success -> handleSelectDirectorySuccess()
                                                    }

                                                    loadingDeviceDisplay = null
                                                }
                                            }) {
                                                Text(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    text = item.upnpDevice.friendlyName
                                                )

                                                val height = 4.dp
                                                Box(modifier = Modifier.height(height)) {
                                                    androidx.compose.animation.AnimatedVisibility(visible = item.isLoading()) {
                                                        LinearProgressIndicator(
                                                            modifier = Modifier
                                                                .height(height)
                                                                .fillMaxWidth()
                                                        )
                                                    }
                                                }

                                                if (contentDirectories.size > 1 && index != contentDirectories.size - 1) {
                                                    Divider(modifier = Modifier.fillMaxWidth())
                                                }
                                            }
                                        }
                                    }
                                )
                        }
                    }
                }
            }
        }
    }

    private fun initUpnpService() {
        val viewModel: SelectContentDirectoryPresenter by viewModels()
        viewModel.pass
    }

    private fun handleGearClick() {
        startActivity(Intent(this, SelectApplicationModeActivity::class.java))
    }

    private fun handleSelectDirectorySuccess() {
        startActivity((application as Router).getMainActivityIntent(this))
    }

    private fun handleSelectDirectoryError() {
        Toast
            .makeText(this, "Failed to connect to content directory", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onBackPressed() {
        finishAndRemoveTask()
    }
}
