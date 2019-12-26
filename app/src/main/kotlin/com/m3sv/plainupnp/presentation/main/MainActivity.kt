package com.m3sv.plainupnp.presentation.main

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.m3sv.plainupnp.App
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.ChangeSettingsMenuStateAction
import com.m3sv.plainupnp.common.HalfClockwiseRotateSlideAction
import com.m3sv.plainupnp.common.utils.enforce
import com.m3sv.plainupnp.common.utils.hideKeyboard
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.databinding.MainActivityBinding
import com.m3sv.plainupnp.di.activity.MainActivitySubComponent
import com.m3sv.plainupnp.presentation.base.ActivityConfig
import com.m3sv.plainupnp.presentation.base.BaseActivity
import com.m3sv.plainupnp.presentation.controls.ControlsAction
import com.m3sv.plainupnp.presentation.controls.ControlsActionCallback
import com.m3sv.plainupnp.presentation.controls.ControlsFragment
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE


private val UpnpRendererState.icon: Int
    inline get() = when (state) {
        UpnpRendererState.State.STOP -> R.drawable.ic_play_arrow
        UpnpRendererState.State.PLAY -> R.drawable.ic_pause
        UpnpRendererState.State.PAUSE -> R.drawable.ic_play_arrow
        UpnpRendererState.State.INITIALIZING -> R.drawable.ic_play_arrow
        UpnpRendererState.State.FINISHED -> R.drawable.ic_play_arrow
    }

class MainActivity : BaseActivity<MainActivityBinding>(),
    Toolbar.OnMenuItemClickListener,
    NavController.OnDestinationChangedListener,
    ControlsActionCallback {

    lateinit var mainActivitySubComponent: MainActivitySubComponent

    override val activityConfig: ActivityConfig = ActivityConfig(R.layout.main_activity)

    private lateinit var viewModel: MainViewModel

    private val bottomNavDrawer: ControlsFragment by lazy(NONE) {
        (supportFragmentManager.findFragmentById(R.id.bottom_nav_drawer) as ControlsFragment)
            .apply { actionCallback = this@MainActivity }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()

        findNavController(R.id.nav_host_container).addOnDestinationChangedListener(this)

        observeState()
        setupBottomNavigation()
        setupBottomNavigationListener()
        requestReadStoragePermission()

        if (savedInstanceState == null) startUpnpService()

        with(bottomNavDrawer) {
            addOnSlideAction(HalfClockwiseRotateSlideAction(binding.bottomAppBarChevron))
            addOnStateChangedAction(ChangeSettingsMenuStateAction { showSettings ->
                if (showSettings) {
                    hideKeyboard()
                    binding.bottomBar.replaceMenu(R.menu.bottom_app_bar_settings_menu)
                } else {
                    binding.bottomBar.replaceMenu(R.menu.bottom_app_bar_home_menu)
                }
            })
        }

        binding.bottomAppBarTitle.setOnClickListener { bottomNavDrawer.toggle() }
        setSupportActionBar(binding.bottomBar)
    }

    private fun setupBottomNavigation() {
        with(binding.bottomBar) {
            setOnMenuItemClickListener(this@MainActivity)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.execute(MainIntention.ResumeUpnp)
    }

    override fun onStop() {
        viewModel.execute(MainIntention.PauseUpnp)
        super.onStop()
    }

    override fun onDestroy() {
        if (isFinishing) viewModel.execute(MainIntention.StopUpnpService)
        super.onDestroy()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                bottomNavDrawer.close()

                findNavController(R.id.nav_host_container).navigate(R.id.action_mainFragment_to_settingsFragment)
            }
        }
        return true
    }

    private fun startUpnpService() {
        viewModel.execute(MainIntention.StartUpnpService)
    }

    private fun setupBottomNavigationListener() {
        NavigationUI.setupWithNavController(
            binding.bottomBar,
            findNavController(R.id.nav_host_container)
        )
    }

    private fun observeState() {
        viewModel.state.nonNullObserve { state ->
            when (state) {
                is MainState.Render -> {
                    with(bottomNavDrawer) {
                        setRenderers(state.renderers)
                        setContentDirectories(state.contentDirectories)
                    }

                    handleRendererState(state.rendererState)
                }
                is MainState.Exit -> finishAndRemoveTask()
            }.enforce
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP -> {
            viewModel.execute(MainIntention.PlayerButtonClick(PlayerButton.RAISE_VOLUME))
            true
        }

        KeyEvent.KEYCODE_VOLUME_DOWN -> {
            viewModel.execute(MainIntention.PlayerButtonClick(PlayerButton.LOWER_VOLUME))
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

//        with(binding.controlsSheet) {
//            play.setImageResource(rendererState.icon)
//        }

//        with(binding.controlsSheet.art) {
//            var thumb: Any? = when (rendererState.type) {
//                UpnpItemType.AUDIO -> R.drawable.ic_music
//                else -> rendererState.uri
//            }
//
//            thumb = thumb ?: R.drawable.ic_launcher_no_shadow
//
//            Glide.with(this).load(thumb).into(this)
//        }

//        binding.controlsSheet.title.text = rendererState.title
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

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_exit_title))
            .setMessage(getString(R.string.dialog_exit_body))
            .setPositiveButton(getString(R.string.exit)) { _, _ ->
                finishAndRemoveTask()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottom_app_bar_home_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_search)?.apply {
            (actionView as SearchView).apply {
                applySearchViewTransitionAnimation()
                setSearchQueryListener()
                disableSearchViewFullScreenEditing()
                animateAppear()
            }

            setOnMenuItemClickListener { item ->
                item.expandActionView()
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                bottomNavDrawer.close()

                findNavController(R.id.nav_host_container).navigate(R.id.action_mainFragment_to_settingsFragment)
            }
        }
        return true
    }

    private fun SearchView.disableSearchViewFullScreenEditing() {
        imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_ACTION_SEARCH
    }

    private fun SearchView.animateAppear() {
        val anim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.slide_up)
        startAnimation(anim)
    }

    private fun SearchView.setSearchQueryListener() {
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean = false

            override fun onQueryTextChange(newText: String): Boolean {
//                contentAdapter.filter(newText)
                Timber.d("New text")
                return true
            }
        })
    }

    private fun SearchView.applySearchViewTransitionAnimation() {
        findViewById<LinearLayout>(R.id.search_bar).layoutTransition = LayoutTransition()
    }

    private fun inject() {
        mainActivitySubComponent =
            (applicationContext as App).appComponent.mainActivitySubComponent().create()
        mainActivitySubComponent.inject(this)
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

        viewModel.execute(intention)
    }
}
