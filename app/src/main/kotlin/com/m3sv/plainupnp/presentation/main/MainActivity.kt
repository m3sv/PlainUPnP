package com.m3sv.plainupnp.presentation.main

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.ROTATION
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.m3sv.plainupnp.App
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.ChangeSettingsMenuStateAction
import com.m3sv.plainupnp.common.ShutdownDispatcher
import com.m3sv.plainupnp.common.Shutdownable
import com.m3sv.plainupnp.common.TriggerOnceStateAction
import com.m3sv.plainupnp.common.utils.enforce
import com.m3sv.plainupnp.common.utils.hideKeyboard
import com.m3sv.plainupnp.data.upnp.UpnpItemType
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.databinding.MainActivityBinding
import com.m3sv.plainupnp.di.main.MainActivitySubComponent
import com.m3sv.plainupnp.presentation.base.BaseActivity
import kotlin.LazyThreadSafetyMode.NONE


class MainActivity : BaseActivity(),
    NavController.OnDestinationChangedListener,
    ControlsActionCallback,
    Shutdownable {

    lateinit var mainActivitySubComponent: MainActivitySubComponent

    private lateinit var viewModel: MainViewModel

    private var bottomBarMenu = R.menu.bottom_app_bar_home_menu

    private val bottomNavDrawer: ControlsFragment by lazy(NONE) { getControlsFragment() }

    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ShutdownDispatcher.addListener(this)

        viewModel = getViewModel()

        findNavController(R.id.nav_host_container).addOnDestinationChangedListener(this)

        observeState()
        setupBottomNavigationListener()
        requestReadStoragePermission()

        if (savedInstanceState == null) {
            startUpnpService()
        } else {
            with(savedInstanceState) {
                restoreChevronState()
                restoreMenu()
            }
        }

        animateBottomDrawChanges()
        binding.controlsContainer.setOnClickListener { bottomNavDrawer.toggle() }
        setSupportActionBar(binding.bottomBar)
    }

    override fun onStart() {
        super.onStart()
        viewModel.intention(MainIntention.ResumeUpnp)
    }

    override fun onStop() {
        viewModel.intention(MainIntention.PauseUpnp)
        super.onStop()
    }

    override fun onDestroy() {
        if (isFinishing) viewModel.intention(MainIntention.StopUpnpService)
        ShutdownDispatcher.removeListener(this)
        super.onDestroy()
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is MainState.Render -> {
                    with(bottomNavDrawer) {
                        setRenderers(state.renderers)
                        setContentDirectories(state.contentDirectories)
                    }
                }
                is MainState.Exit -> finishAndRemoveTask()
                is MainState.Initial -> {
                    // ignore
                }
            }.enforce
        }

        viewModel.upnpState().observe(this) { upnpRendererState ->
            handleRendererState(upnpRendererState)
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (destination.id) {
            R.id.home_fragment -> {
                setupBottomAppBarForHome()
            }

            R.id.settings_fragment -> {
                setupBottomAppBarForSettings()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(bottomBarMenu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        setupSearchMenuItem(menu, true)
        return true
    }

    private fun setupSearchMenuItem(menu: Menu, animate: Boolean) {
        menu.findItem(R.id.menu_search)?.let { item ->
            (item.actionView as SearchView).apply {
                setSearchQueryListener()
                disableSearchViewFullScreenEditing()
                if (animate) animateAppear()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                bottomNavDrawer.close()
                findNavController(R.id.nav_host_container).navigate(R.id.action_mainFragment_to_settingsFragment)
            }
            R.id.menu_search -> item.expandActionView()
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putFloat(CHEVRON_ROTATION_ANGLE_KEY, binding.bottomAppBarChevron.rotation)
        outState.putInt(OPTIONS_MENU_KEY, bottomBarMenu)
    }

    override fun onAction(action: ControlsAction) {
        val intention = when (action) {
            is ControlsAction.NextClick -> MainIntention.PlayerButtonClick(PlayerButton.NEXT)
            is ControlsAction.PreviousClick -> MainIntention.PlayerButtonClick(PlayerButton.PREVIOUS)
            is ControlsAction.PlayClick -> MainIntention.PlayerButtonClick(PlayerButton.PLAY)
            is ControlsAction.ProgressChange -> MainIntention.MoveTo(action.progress)
            is ControlsAction.SelectRenderer -> MainIntention.SelectRenderer(action.position)
            is ControlsAction.SelectContentDirectory -> MainIntention.SelectContentDirectory(action.position)
        }

        viewModel.intention(intention)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP -> {
            viewModel.intention(MainIntention.PlayerButtonClick(PlayerButton.RAISE_VOLUME))
            true
        }

        KeyEvent.KEYCODE_VOLUME_DOWN -> {
            viewModel.intention(MainIntention.PlayerButtonClick(PlayerButton.LOWER_VOLUME))
            true
        }
        else -> super.onKeyDown(keyCode, event)
    }

    private fun handleRendererState(rendererState: UpnpRendererState?) {
        if (rendererState == null) return

        bottomNavDrawer.setProgress(
            rendererState.elapsedPercent,
            rendererState.state == UpnpRendererState.State.PLAY
        )

        bottomNavDrawer.setPlayIcon(rendererState.icon)
        bottomNavDrawer.setTitle(rendererState.title)

        when (rendererState.type) {
            UpnpItemType.AUDIO -> bottomNavDrawer.setThumbnail(R.drawable.ic_music)
            else -> rendererState.uri?.let(bottomNavDrawer::setThumbnail)
        }
    }

    private fun animateBottomDrawChanges() {
        with(bottomNavDrawer) {
            addOnStateChangedAction(TriggerOnceStateAction(this@MainActivity::animateChevronArrow))
            addOnStateChangedAction(ChangeSettingsMenuStateAction(this@MainActivity::replaceAppBarMenu))
        }
    }

    private fun replaceAppBarMenu(showSettings: Boolean) {
        bottomBarMenu = if (showSettings) {
            hideKeyboard()
            R.menu.bottom_app_bar_settings_menu
        } else {
            R.menu.bottom_app_bar_home_menu
        }

        with(binding.bottomBar) {
            replaceMenu(bottomBarMenu)
            setupSearchMenuItem(menu, false)
        }
    }

    private val arrowUpAnimator by lazy(mode = NONE) {
        ObjectAnimator.ofFloat(binding.bottomAppBarChevron, ROTATION, 0f)
            .apply {
                duration = 200
            }
    }

    private val arrowDownAnimator by lazy(mode = NONE) {
        ObjectAnimator.ofFloat(binding.bottomAppBarChevron, ROTATION, 180f)
            .apply {
                duration = 200
            }
    }

    private fun animateChevronArrow(isHidden: Boolean) {
        if (isHidden) {
            arrowUpAnimator.start()
        } else {
            arrowDownAnimator.start()
        }
    }

    private fun getControlsFragment(): ControlsFragment =
        (supportFragmentManager.findFragmentById(R.id.bottom_nav_drawer) as ControlsFragment).apply {
            actionCallback = this@MainActivity
        }

    private fun setupBottomNavigationListener() {
        NavigationUI.setupWithNavController(
            binding.bottomBar,
            findNavController(R.id.nav_host_container)
        )
    }

    private fun startUpnpService() {
        viewModel.intention(MainIntention.StartUpnpService)
    }

    private fun inject() {
        mainActivitySubComponent =
            (applicationContext as App).appComponent.mainActivitySubComponent().create()
        mainActivitySubComponent.inject(this)
    }

    override fun shutdown() {
        finishAndRemoveTask()
    }

    private fun setupBottomAppBarForHome() {
        showAppBarWithAnimation()
    }

    private fun setupBottomAppBarForSettings() {
        hideAppBar()
    }

    private fun showAppBarWithAnimation() {
        with(binding.bottomBar) {
            visibility = View.VISIBLE
            performShow()
        }
    }

    private fun hideAppBar() {
        with(binding.bottomBar) {
            performHide()
            visibility = View.GONE
        }
    }

    private fun Bundle.restoreChevronState() {
        binding.bottomAppBarChevron.rotation = getFloat(CHEVRON_ROTATION_ANGLE_KEY, 0f)
    }

    private fun Bundle.restoreMenu() {
        bottomBarMenu = getInt(OPTIONS_MENU_KEY, R.menu.bottom_app_bar_home_menu)
    }

    private fun SearchView.setSearchQueryListener() {
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean = false

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.intention(MainIntention.Filter(newText))
                return true
            }
        })
    }

    private companion object {
        private const val CHEVRON_ROTATION_ANGLE_KEY = "chevron_rotation_angle_key"
        private const val OPTIONS_MENU_KEY = "options_menu_key"
    }
}

private fun SearchView.disableSearchViewFullScreenEditing() {
    imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_ACTION_SEARCH
}

private fun View.animateAppear() {
    val anim = AnimationUtils.loadAnimation(context, R.anim.slide_up)
    startAnimation(anim)
}

private val UpnpRendererState.icon: Int
    inline get() = when (state) {
        UpnpRendererState.State.STOP -> R.drawable.ic_play_arrow
        UpnpRendererState.State.PLAY -> R.drawable.ic_pause
        UpnpRendererState.State.PAUSE -> R.drawable.ic_play_arrow
        UpnpRendererState.State.INITIALIZING -> R.drawable.ic_play_arrow
        UpnpRendererState.State.FINISHED -> R.drawable.ic_play_arrow
    }
