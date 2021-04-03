package com.m3sv.selectcontentdirectory

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.m3sv.plainupnp.Router
import com.m3sv.plainupnp.common.util.pass
import com.m3sv.plainupnp.compose.util.AppTheme
import com.m3sv.plainupnp.compose.widgets.OnePane
import com.m3sv.plainupnp.compose.widgets.OneTitle
import com.m3sv.plainupnp.compose.widgets.OneToolbar
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.upnp.manager.Result
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectContentDirectoryActivity : AppCompatActivity() {

    @Inject
    lateinit var upnpManager: UpnpManager

    private val presenter: SelectContentDirectoryPresenter by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Trigger lazy initialization
        presenter.pass

        setContent {
            val contentDirectories by upnpManager.contentDirectories.collectAsState(initial = listOf())
            var loadingDeviceDisplay: DeviceDisplay? by remember { mutableStateOf(null) }

            fun DeviceDisplay.isLoading(): Boolean = loadingDeviceDisplay != null && loadingDeviceDisplay == this

            AppTheme {
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
                        Card(modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)) {
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
                                            Column(modifier = Modifier
                                                .clickable(enabled = loadingDeviceDisplay == null) {
                                                    loadingDeviceDisplay = item
                                                    lifecycleScope.launch {
                                                        when (upnpManager.selectContentDirectoryAsync(item.upnpDevice)
                                                            .await()) {
                                                            Result.Error -> handleSelectDirectoryError()
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

                                                if (item.isLoading()) {
                                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                                }

                                                if (contentDirectories.size > 1 && index != contentDirectories.size - 1) {
                                                    Divider(color = Color.DarkGray)
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

    private fun handleGearClick() {
        startActivity(Intent(this, SelectApplicationModeActivity::class.java))
    }

    private fun handleSelectDirectorySuccess() {
        startActivity((application as Router).getNextIntent(this))
    }

    private fun handleSelectDirectoryError() {
        Toast
            .makeText(this, "Failed to connect to content directory", Toast.LENGTH_SHORT)
            .show()
    }

}
