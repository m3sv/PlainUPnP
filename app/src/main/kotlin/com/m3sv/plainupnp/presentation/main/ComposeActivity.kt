package com.m3sv.plainupnp.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.m3sv.plainupnp.App
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.presentation.main.di.MainActivitySubComponent
import com.m3sv.plainupnp.presentation.settings.SettingsActivity
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class ComposeActivity : AppCompatActivity() {

    lateinit var mainActivitySubComponent: MainActivitySubComponent

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: MainViewModel by lazy(NONE) {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }
    }

    private fun inject() {
        mainActivitySubComponent = (applicationContext as App)
            .appComponent
            .mainSubComponent()
            .create()
            .also { component -> component.inject(this) }
    }

    @Composable
    private fun Content() {
        val context = AmbientContext.current
        val renderers by viewModel.renderers.observeAsState()
        val contentDirectories by viewModel.contentDirectories.observeAsState()

        MaterialTheme(if (isSystemInDarkTheme()) darkColors() else lightColors()) {
            val vectorDrawable = vectorResource(id = R.drawable.ic_settings)

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {

                Column(modifier = Modifier.align(Alignment.TopCenter)) {
                    Row(horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = {
                            startActivity(Intent(context, SettingsActivity::class.java))
                        }) {
                            Icon(vectorDrawable, tint = MaterialTheme.colors.onSurface)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        DeviceSelectionDropdown(
                            deviceBundle = contentDirectories,
                            name = "ContentDirectories",
                            icon = R.drawable.ic_content_directory,
                            modifier = Modifier.weight(1f),
                            context = context,
                        )

                        DeviceSelectionDropdown(
                            deviceBundle = renderers,
                            name = "Renderers",
                            icon = R.drawable.ic_renderer,
                            modifier = Modifier.weight(1f),
                            context = context
                        )
                    }
                }

            }
        }
    }

    @Composable
    private fun DeviceSelectionDropdown(
        deviceBundle: SpinnerItemsBundle?,
        name: String,
        @DrawableRes icon: Int,
        modifier: Modifier,
        context: Context,
    ) {
        var expanded by remember { mutableStateOf(false) }
        var selectedDir by remember { mutableStateOf(name) }

        Surface(
            modifier = modifier
                .clickable(onClick = {
                    expanded = true
                })
                .wrapContentHeight(),
            color = Color.Yellow) {
            DropdownMenu(
                toggle = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(vectorResource(id = icon), modifier = Modifier.padding(16.dp))
                        Text(
                            text = selectedDir,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                },
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                deviceBundle?.devices?.forEachIndexed { i, item ->
                    DropdownMenuItem(onClick = {
                        expanded = false
                        selectedDir = item.toString()
                        Toast.makeText(
                            context,
                            "Selected $item, position $i",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Text(item.toString())
                    }
                }
            }
        }
    }
}
