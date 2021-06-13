package com.m3sv.plainupnp.presentation.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.glide.rememberGlidePainter
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.ThemeManager
import com.m3sv.plainupnp.compose.util.AppTheme
import com.m3sv.plainupnp.compose.widgets.OneToolbar
import com.m3sv.plainupnp.core.eventbus.events.ExitApplication
import com.m3sv.plainupnp.core.eventbus.subscribe
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.presentation.settings.SettingsActivity
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl.Companion.USER_DEFINED_PREFIX
import com.m3sv.plainupnp.upnp.didl.ClingContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.didl.ClingMedia
import com.m3sv.plainupnp.upnp.folder.Folder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.fourthline.cling.support.model.TransportState
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    private val viewModel: MainViewModel by viewModels()

    private var isConnectedToRenderer: Boolean = false

    private val progressIndicatorSize = 4.dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var selectedRenderer by rememberSaveable { mutableStateOf("Stream to") }
            var isDialogExpanded by rememberSaveable { mutableStateOf(false) }
            var isButtonExpanded by rememberSaveable { mutableStateOf(true) }
            var showFilter by rememberSaveable { mutableStateOf(false) }

            val viewState by viewModel.viewState.collectAsState()
            val volume by viewModel.volume.collectAsState()
            val folders: List<Folder> by viewModel.navigation.collectAsState()
            val filterText by viewModel.filterText.collectAsState()
            val loading by viewModel.loading.collectAsState()
            val currentTheme by themeManager.collectTheme()

            fun clearFilterText() {
                viewModel.filterInput("")
            }

            val onFilterClick: () -> Unit = {
                showFilter = !showFilter

                if (!showFilter) {
                    clearFilterText()
                }
            }

            val onSettingsClick: () -> Unit = {
                openSettings()
            }

            fun collapseExpandedButton() {
                isButtonExpanded = false
                isDialogExpanded = false
            }

            @Composable
            fun createFloatingActionButton() {
                RendererFloatingActionButton(
                    isButtonExpanded = isButtonExpanded,
                    isDialogExpanded = isDialogExpanded,
                    selectedRenderer = selectedRenderer,
                    renderers = viewState.renderers,
                    onDismissDialog = {
                        isDialogExpanded = false
                        collapseExpandedButton()
                    },
                    onExpandButton = { isButtonExpanded = true },
                    onSelectRenderer = { name ->
                        selectedRenderer = name
                    }, onExpandDialog = {
                        isDialogExpanded = true
                    })
            }

            val filter: @Composable () -> Unit = {
                AnimatedVisibility(visible = showFilter) {
                    Filter(
                        initialValue = filterText,
                        onValueChange = { viewModel.filterInput(it) },
                    ) {
                        showFilter = false
                        clearFilterText()
                    }
                }
            }

            val configuration = LocalConfiguration.current

            AppTheme(currentTheme.isDarkTheme()) {
                val showControls = viewState.upnpRendererState !is UpnpRendererState.Empty

                Surface {
                    Box {
                        when (configuration.orientation) {
                            Configuration.ORIENTATION_LANDSCAPE -> {
                                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                                Landscape(
                                    upnpState = viewState.upnpRendererState,
                                    folders = folders,
                                    loading = loading,
                                    showThumbnails = viewState.enableThumbnails,
                                    showControls = showControls,
                                    floatingActionButton = { createFloatingActionButton() },
                                    filter = filter,
                                    onFilterClick = onFilterClick,
                                    onSettingsClick = onSettingsClick,
                                )
                            }
                            else -> {
                                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                                Portrait(
                                    upnpState = viewState.upnpRendererState,
                                    folders = folders,
                                    loading = loading,
                                    showThumbnails = viewState.enableThumbnails,
                                    showControls = showControls,
                                    floatingActionButton = { createFloatingActionButton() },
                                    filter = filter,
                                    onFilterClick = onFilterClick,
                                    onSettingsClick = onSettingsClick,
                                )
                            }
                        }

                        Volume(
                            volumeUpdate = volume,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.finishActivityFlow.collect { finish() }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.isConnectedToRenderer.collect { isConnectedToRenderer = it }
        }

        lifecycleScope.launchWhenCreated {
            subscribe<ExitApplication>().collect { finishAffinity() }
        }
    }

    @Composable
    private fun Volume(volumeUpdate: VolumeUpdate, modifier: Modifier = Modifier) {
        AnimatedVisibility(
            modifier = modifier,
            visible = volumeUpdate is VolumeUpdate.Show
        ) {
            Card(
                shape = RoundedCornerShape(
                    topEnd = 32.dp,
                    bottomEnd = 32.dp
                ),
                elevation = 8.dp
            ) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    Row {
                        val volume = volumeUpdate.volume
                        val icon = when {
                            volume < 5 -> R.drawable.ic_volume_mute
                            volume < 35 -> R.drawable.ic_volume_down
                            volume >= 35 -> R.drawable.ic_volume_up
                            else -> R.drawable.ic_volume_up
                        }

                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null
                        )

                        Text("Volume", Modifier.padding(horizontal = 8.dp))
                        Text("$volume", modifier = Modifier.defaultMinSize(minWidth = 24.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun Portrait(
        upnpState: UpnpRendererState,
        folders: List<Folder>,
        showControls: Boolean,
        loading: Boolean,
        showThumbnails: Boolean,
        onFilterClick: () -> Unit,
        onSettingsClick: () -> Unit,
        floatingActionButton: @Composable BoxScope.() -> Unit,
        filter: @Composable () -> Unit,
    ) {
        Column {
            Toolbar(
                onSettingsClick = onSettingsClick,
                onFilterClick = onFilterClick
            )

            AnimatedVisibility(visible = folders.isNotEmpty()) {
                NavigationBar(folders, modifier = Modifier.padding(start = 16.dp))
            }

            LoadingIndicator(loading)

            Row(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (folders.isNotEmpty()) {
                        Folders(
                            contents = folders.last().folderModel.contents,
                            showThumbnails = showThumbnails
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = !showControls,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        floatingActionButton()
                    }
                }
            }

            filter()

            AnimatedVisibility(visible = showControls) {
                Controls(upnpState, 16.dp, 16.dp)
            }
        }
    }

    @Composable
    private fun Landscape(
        upnpState: UpnpRendererState,
        folders: List<Folder>,
        showControls: Boolean,
        loading: Boolean,
        showThumbnails: Boolean,
        onFilterClick: () -> Unit,
        onSettingsClick: () -> Unit,
        floatingActionButton: @Composable () -> Unit,
        filter: @Composable () -> Unit,
    ) {
        Column {
            Toolbar(
                onSettingsClick = onSettingsClick,
                onFilterClick = onFilterClick
            ) {
                NavigationBar(folders)
            }

            LoadingIndicator(loading)

            val transition = updateTransition(targetState = showControls, label = "")
            val folderWeight by transition.animateFloat(label = "") { showControls ->
                if (showControls)
                    1f
                else
                    10f
            }

            val controlsWeight by transition.animateFloat(label = "") { showControls ->
                if (showControls)
                    1f
                else
                    3f
            }

            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(folderWeight)) {
                    AnimatedVisibility(
                        visible = folders.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Folders(
                            contents = folders.last().folderModel.contents,
                            showThumbnails = showThumbnails
                        )
                    }

                    filter()
                }

                Crossfade(
                    targetState = showControls,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(controlsWeight)
                ) { showControls ->
                    if (showControls) {
                        Controls(upnpState, 0.dp, 8.dp)
                    } else {
                        Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.Bottom) {
                            Spacer(modifier = Modifier.weight(1f))
                            floatingActionButton()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LoadingIndicator(loading: Boolean) {
        Box(modifier = Modifier.height(progressIndicatorSize)) {
            AnimatedVisibility(visible = loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .height(progressIndicatorSize)
                        .fillMaxWidth()
                )
            }
        }
    }

    @Composable
    private fun Folders(
        contents: List<ClingDIDLObject>,
        showThumbnails: Boolean,
    ) {
        LazyColumn {
            items(contents) { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.itemClick(item.id) }
                ) {
                    Spacer(
                        modifier = Modifier.padding(8.dp)
                    )
                    val imageModifier = Modifier.size(32.dp)
                    when (item) {
                        is ClingContainer -> {
                            Image(
                                painterResource(id = R.drawable.ic_folder_24dp),
                                contentDescription = null,
                                modifier = imageModifier
                            )
                        }
                        is ClingMedia -> {
                            when (item) {
                                is ClingMedia.Audio -> Image(
                                    painterResource(id = R.drawable.ic_music),
                                    contentDescription = null,
                                    imageModifier
                                )
                                is ClingMedia.Image -> Image(
                                    if (showThumbnails) {
                                        rememberGlidePainter(
                                            item.uri
                                        )
                                    } else {
                                        painterResource(id = R.drawable.ic_image)
                                    },
                                    contentDescription = null,
                                    imageModifier
                                )
                                is ClingMedia.Video -> Image(
                                    if (showThumbnails) {
                                        rememberGlidePainter(
                                            item.uri
                                        )
                                    } else {
                                        painterResource(id = R.drawable.ic_video)
                                    },
                                    contentDescription = null,
                                    imageModifier
                                )
                            }
                        }
                    }

                    val title = if (item is ClingContainer) {
                        item.title.replace(USER_DEFINED_PREFIX, "")
                    } else {
                        item.title
                    }

                    Text(
                        text = title,
                        maxLines = 1,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.subtitle1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    @Composable
    private fun NavigationBar(folders: List<Folder>, modifier: Modifier = Modifier) {
        LazyRow(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            itemsIndexed(folders) { index, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.navigateTo(item)
                    }
                ) {
                    val labelColor: Color
                    val arrowColor: Color

                    if (index == folders.size - 1) {
                        labelColor = MaterialTheme.colors.primary
                        arrowColor = MaterialTheme.colors.primary
                    } else {
                        labelColor = Color.Unspecified
                        arrowColor = MaterialTheme.colors.onSurface
                    }

                    if (index == 0) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_folder_home),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                        )
                    } else {
                        Image(
                            painterResource(id = R.drawable.ic_next_folder),
                            null,
                            colorFilter = ColorFilter.tint(arrowColor)
                        )
                    }

                    Box {
                        Text(
                            text = item.folderModel.title,
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold,
                            color = labelColor,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun Controls(upnpState: UpnpRendererState, elevation: Dp, verticalPadding: Dp = 0.dp) {
        val defaultState: UpnpRendererState.Default? = upnpState as? UpnpRendererState.Default

        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            elevation = elevation,
        ) {
            Row {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = verticalPadding
                            )
                    ) {
                        Text(defaultState?.title ?: "")

                        Surface {

                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        viewModel.playerButtonClick(PlayerButton.STOP)
                                    }
                            )
                        }

                    }

                    Row {
                        Slider(value = (defaultState?.elapsedPercent ?: 0).toFloat() / 100, onValueChange = {
                            viewModel.moveTo((it * 100).toInt())
                        })
                    }

                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Text(defaultState?.position ?: "00:00")
                        Text("/")
                        Text(defaultState?.duration ?: "00:00")
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val iconSize = Modifier.size(32.dp)
                        Image(
                            painter = painterResource(id = R.drawable.ic_skip_previous),
                            contentDescription = null,
                            modifier = iconSize.clickable {
                                viewModel.playerButtonClick(PlayerButton.PREVIOUS)
                            }
                        )
                        Image(
                            painter = painterResource(id = defaultState?.icon ?: R.drawable.ic_play_arrow),
                            contentDescription = null,
                            modifier = iconSize.clickable {
                                viewModel.playerButtonClick(PlayerButton.PLAY)
                            }
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_skip_next),
                            contentDescription = null,
                            modifier = iconSize.clickable {
                                viewModel.playerButtonClick(PlayerButton.NEXT)
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun RendererFloatingActionButton(
        isButtonExpanded: Boolean,
        isDialogExpanded: Boolean,
        selectedRenderer: String,
        renderers: SpinnerItemsBundle,
        onDismissDialog: () -> Unit,
        onExpandButton: () -> Unit,
        onSelectRenderer: (String) -> Unit,
        onExpandDialog: () -> Unit,
    ) {
        Box {
            FloatingActionButton(onClick = {
                if (isButtonExpanded)
                    onExpandDialog()
                else {
                    onExpandButton()
                }
            }, modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(painterResource(id = R.drawable.ic_cast), null)
                    // Toggle the visibility of the content with animation.
                    AnimatedVisibility(visible = isButtonExpanded) {
                        Text(
                            text = selectedRenderer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp, top = 3.dp)
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = isDialogExpanded,
                onDismissRequest = onDismissDialog,
                offset = DpOffset((16).dp, (0).dp),
            ) {
                for (item in renderers.devices) {
                    DropdownMenuItem(onClick = {
                        onDismissDialog()
                        onSelectRenderer(item.name)

                        viewModel.selectRenderer(item)
                    }) {
                        Text(text = item.name)
                    }
                }
            }
        }
    }

    @Composable
    private fun Toolbar(
        onSettingsClick: () -> Unit,
        onFilterClick: () -> Unit,
        content: @Composable RowScope.() -> Unit = {}
    ) {
        Box {
            OneToolbar(onBackClick = { onBackPressed() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    content()
                    Spacer(modifier = Modifier.weight(1f))
                    SettingsMenu(
                        onSettingsClick = onSettingsClick,
                        onFilterClick = onFilterClick
                    )
                }
            }
        }
    }

    @Composable
    private fun SettingsMenu(onSettingsClick: () -> Unit, onFilterClick: () -> Unit) {
        var expanded by remember { mutableStateOf(false) }

        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.75f)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset((-100).dp, 0.dp),
        ) {
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    onFilterClick()
                }
            ) {
                Text(stringResource(id = R.string.search))
            }

            DropdownMenuItem(
                onClick = {
                    expanded = false
                    onSettingsClick()
                }
            ) {
                Text(stringResource(R.string.title_feature_settings))
            }
        }
    }

    @Composable
    private fun Filter(
        initialValue: String,
        onValueChange: (String) -> Unit,
        onCloseClick: () -> Unit
    ) {
        OutlinedTextField(
            value = initialValue,
            onValueChange = onValueChange,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            onCloseClick()
                        }
                )
            }
        )
    }

    private fun openSettings() {
        lifecycleScope.launch(Dispatchers.IO) {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (isConnectedToRenderer) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    viewModel.playerButtonClick(PlayerButton.RAISE_VOLUME)
                    true
                }

                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    viewModel.playerButtonClick(PlayerButton.LOWER_VOLUME)
                    true
                }
                else -> super.onKeyDown(keyCode, event)
            }
        } else super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        viewModel.navigateBack()
    }

    private val UpnpRendererState.Default.icon: Int
        inline get() = when (state) {
            TransportState.PLAYING -> R.drawable.ic_pause
            TransportState.STOPPED,
            TransportState.TRANSITIONING,
            TransportState.PAUSED_PLAYBACK,
            TransportState.PAUSED_RECORDING,
            TransportState.RECORDING,
            TransportState.NO_MEDIA_PRESENT,
            TransportState.CUSTOM,
            -> R.drawable.ic_play_arrow
        }
}
