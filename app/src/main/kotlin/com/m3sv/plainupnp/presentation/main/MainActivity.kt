package com.m3sv.plainupnp.presentation.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
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
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.compose.util.AppTheme
import com.m3sv.plainupnp.compose.widgets.OneToolbar
import com.m3sv.plainupnp.core.eventbus.events.ExitApplication
import com.m3sv.plainupnp.core.eventbus.subscribe
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.presentation.settings.SettingsActivity
import com.m3sv.plainupnp.upnp.didl.ClingContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.didl.ClingMedia
import com.m3sv.plainupnp.upnp.folder.Folder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.fourthline.cling.support.model.TransportState
import kotlin.LazyThreadSafetyMode.NONE

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val volumeIndicator: VolumeIndicator by lazy(NONE) { VolumeIndicator(this) }

    private var isConnectedToRenderer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                var showControls by remember { mutableStateOf(false) }

                val renderers by viewModel.renderers.collectAsState(SpinnerItemsBundle.empty)

                val upnpState by viewModel.upnpState
                    .onEach { showControls = it !is UpnpRendererState.Empty }
                    .collectAsState(initial = UpnpRendererState.Empty)

                val navigationStack by viewModel.navigationStack.collectAsState(listOf(Folder.Empty))

                var selectedRenderer by rememberSaveable { mutableStateOf("Stream to") }
                var isDialogExpanded by rememberSaveable { mutableStateOf(false) }
                var isButtonExpanded by rememberSaveable { mutableStateOf(true) }

                fun collapseExpandedButton() {
                    isButtonExpanded = false
                    isDialogExpanded = false
                }

                @Composable
                fun BoxScope.createFloatingActionButton() {
                    RendererFloatingActionButton(
                        isButtonExpanded = isButtonExpanded,
                        isDialogExpanded = isDialogExpanded,
                        selectedRenderer = selectedRenderer,
                        renderers = renderers,
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


                if (navigationStack.isEmpty()) {
                    finish()
                }

                val configuration = LocalConfiguration.current

                when (configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> Landscape(
                        navigationStack = navigationStack,
                        floatingActionButton = { createFloatingActionButton() },
                        controls = {
                            AnimatedVisibility(
                                visible = showControls,
                                enter = fadeIn() + expandHorizontally(Alignment.Start),
                                exit = fadeOut() + shrinkHorizontally(Alignment.Start),
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .align(Alignment.Top)
                            ) {
                                Controls(upnpState, 0.dp, 8.dp)
                            }
                        })
                    else -> Portrait(
                        navigationStack = navigationStack,
                        floatingActionButton = { createFloatingActionButton() },
                        controls = {
                            AnimatedVisibility(visible = showControls) {
                                Controls(upnpState, 16.dp, 16.dp)
                            }
                        }
                    )
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            subscribe<ExitApplication>().collect { finishAffinity() }
        }
    }

    @Composable
    private fun Screen(navigationStack: List<Folder>, body: @Composable ColumnScope.() -> Unit) {
        Column {
            Toolbar()
            NavigationBar(navigationStack)
            body()
        }
    }

    @Composable
    private fun Portrait(
        navigationStack: List<Folder>,
        controls: @Composable ColumnScope.() -> Unit,
        floatingActionButton: @Composable BoxScope.() -> Unit,
    ) {
        Screen(navigationStack) {
            Row(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Folder(navigationStack.lastOrNull()?.contents ?: listOf())
                    floatingActionButton()
                }
            }

            controls()
        }
    }

    @Composable
    private fun Landscape(
        navigationStack: List<Folder>,
        controls: @Composable RowScope.() -> Unit,
        floatingActionButton: @Composable BoxScope.() -> Unit,
    ) {
        Screen(navigationStack) {
            Row {
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)) {
                    Folder(navigationStack.lastOrNull()?.contents ?: listOf())
                    floatingActionButton()
                }

                controls()
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

                        Image(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    viewModel.playerButtonClick(PlayerButton.STOP)
                                }
                        )
                    }

//                    upnpState.artist
//                        ?.takeIf { it.isNotBlank() }
//                        ?.let { artist -> Row { Text(artist) } }

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
    private fun BoxScope.RendererFloatingActionButton(
        isButtonExpanded: Boolean,
        isDialogExpanded: Boolean,
        selectedRenderer: String,
        renderers: SpinnerItemsBundle,
        onDismissDialog: () -> Unit,
        onExpandButton: () -> Unit,
        onSelectRenderer: (String) -> Unit,
        onExpandDialog: () -> Unit,
    ) {
        Box(modifier = Modifier.Companion.align(Alignment.BottomEnd)) {
            FloatingActionButton(onClick = {
                if (isButtonExpanded)
                    onExpandDialog()
                else {
                    onExpandButton()
                }
            }, modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(painterResource(id = R.drawable.ic_cast), null)
                    // Toggle the visibility of the content with animation.
                    AnimatedVisibility(visible = isButtonExpanded) {
                        Text(
                            text = selectedRenderer,
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
    private fun NavigationBar(stack: List<Folder>) {
        Surface {
            LazyRow(verticalAlignment = Alignment.CenterVertically) {
                itemsIndexed(stack) { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            viewModel.navigateTo(item)
                        }
                    ) {
                        val labelColor: Color
                        val arrowColor: Color

                        if (index == stack.size - 1) {
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
                                modifier = Modifier.padding(start = 16.dp, end = 4.dp)
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
                                text = item.title,
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
    }

    @Composable
    private fun Toolbar() {
        Row {
            Surface {
                Box {
                    OneToolbar(onBackClick = { onBackPressed() }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsMenu(
                                onSettingsClick = {
                                    openSettings()
                                },
                                onFilterClick = {
                                    //                                            showSearch()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Folder(contents: List<ClingDIDLObject>) {
        Surface {
            LazyColumn {
                items(contents) { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.itemClick(item)
                            }
                    ) {
                        Spacer(modifier = Modifier.padding(8.dp))

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
                                        painterResource(id = R.drawable.ic_image),
                                        contentDescription = null,
                                        imageModifier
                                    )
                                    is ClingMedia.Video -> Image(
                                        painterResource(id = R.drawable.ic_video),
                                        contentDescription = null,
                                        imageModifier
                                    )
                                }
                            }
                        }

                        Text(
                            text = item.title,
                            maxLines = 1,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.subtitle1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
//
//    private fun navigateToFolder(folder: Folder) {
//        when (folder) {
//            is Folder.Root -> viewModel.navigate(MainRoute.ToFolder(folder))
//            is Folder.SubFolder -> viewModel.navigate(MainRoute.Back(folder))
//        }
//    }
//
//    private fun hideSearchContainer() {
//        with(binding) {
//            val currentFocus = currentFocus
//            if (inputMethodManager.isActive(searchInput) && currentFocus != null) {
//                inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
//            }
//            searchInput.clearFocus()
//            searchContainer.visibility = View.INVISIBLE
//            searchInput.setText("")
//        }
//    }

    private fun observeState() {

        lifecycleScope.launchWhenCreated {
            viewModel.volume.collect { volume: Int ->
                volumeIndicator.volume = volume
            }
        }

//        viewModel
//            .changeFolder
//            .observe(this) { changeEvent ->
////                areControlsVisible = true
//                changeEvent.consume { folderType ->
//                    when (folderType) {
//                        is Folder.Root -> {
//                            supportFragmentManager.popBackStack(
//                                null,
//                                POP_BACK_STACK_INCLUSIVE
//                            )
//                            replaceFragment(HomeFragment(), folderType.id)
//                        }
//
//                        is Folder.SubFolder -> replaceFragment(
//                            HomeFragment(),
//                            folderType.id,
//                            true
//                        )
//                    }
//                }
//            }

//        viewModel
//            .navigationStrip
//            .observe(this) { folders ->
//                binding.navigationStrip.replaceItems(folders)
//
//                val clickListener = folders
//                    .firstOrNull()
//                    ?.let { folder -> View.OnClickListener { navigateToFolder(folder) } }
//
//                binding.navigateHome.setOnClickListener(clickListener)
//            }
//        viewModel.isConnectedToRenderer.asLiveData().observe(this) {
//            isConnectedToRenderer = it != null
//        }

//        viewModel.navigation.observe(this) { navigationEvent ->
//            navigationEvent.consume { route ->
//                when (route) {
//                    is MainRoute.Back -> {
//                        supportFragmentManager.popBackStack(route.folder?.id, 0)
//                        areControlsVisible = true
//                    }
//                    is MainRoute.ToFolder -> pass
//                    is MainRoute.PreviewImage -> {
//                        areControlsVisible = false
//                        replaceFragment(ImageFragment.newInstance(route.url),
//                            addToBackStack = true)
//                    }
//                    is MainRoute.PreviewVideo -> {
//                        areControlsVisible = false
//                        replaceFragment(PlayerFragment.newInstance(route.url),
//                            addToBackStack = true)
//                    }
//                    is MainRoute.PreviewAudio -> {
//                        areControlsVisible = false
//                        replaceFragment(PlayerFragment.newInstance(route.url),
//                            addToBackStack = true)
//                    }
//                    is MainRoute.Initial -> pass
//                }.exhaustive
//            }
//        }
    }

//    private var areControlsVisible: Boolean by Delegates.observable(true) { _, _, visible ->
//        if (visible) {
//            with(binding) {
//                bottomBar.performShow()
//                navigationStripContainer.visibility = View.VISIBLE
//            }
//        } else {
//            hideSearchContainer()
//            with(binding) {
//                controlsFragment.close()
//                bottomBar.performHide()
//                navigationStripContainer.visibility = View.GONE
//            }
//        }
//    }

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

//    private fun animateBottomDrawChanges() {
//        controlsFragment.addOnStateChangedAction(TriggerOnceStateAction(this@MainActivity::animateChevronArrow))
//    }
//
//    private val arrowUpAnimator by lazy(mode = NONE) {
//        ObjectAnimator
//            .ofFloat(binding.bottomAppBarChevron, ROTATION, 0f)
//            .apply { duration = 200 }
//    }
//
//    private val arrowDownAnimator by lazy(mode = NONE) {
//        ObjectAnimator
//            .ofFloat(binding.bottomAppBarChevron, ROTATION, 180f)
//            .apply { duration = 200 }
//    }

//    private fun animateChevronArrow(isHidden: Boolean) {
//        val animator = if (isHidden) {
//            arrowUpAnimator
//        } else {
//            arrowDownAnimator
//        }
//
//        animator.start()
//    }
//
//    private fun Bundle.restoreChevronState() {
//        binding.bottomAppBarChevron.rotation = getFloat(CHEVRON_ROTATION_ANGLE_KEY, 0f)
//    }
//
//    private fun Bundle.restoreSearchContainerVisibility() {
//        val isSearchContainerVisible = getBoolean(IS_SEARCH_CONTAINER_VISIBLE, false)
//        binding
//            .searchContainer
//            .visibility = if (isSearchContainerVisible) View.VISIBLE else View.INVISIBLE
//    }
//
//    private fun Bundle.restoreControlsVisibility() {
//        areControlsVisible = getBoolean(ARE_CONTROLS_VISIBLE, false)
//    }

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
