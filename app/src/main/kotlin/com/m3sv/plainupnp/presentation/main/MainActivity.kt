package com.m3sv.plainupnp.presentation.main

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
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
import com.google.android.material.bottomappbar.BottomAppBar
import com.m3sv.plainupnp.App
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.ChangeSettingsMenuStateAction
import com.m3sv.plainupnp.common.TriggerOnceStateAction
import com.m3sv.plainupnp.common.VolumeIndicator
import com.m3sv.plainupnp.common.utils.hideKeyboard
import com.m3sv.plainupnp.databinding.MainActivityBinding
import com.m3sv.plainupnp.di.main.MainActivitySubComponent
import com.m3sv.plainupnp.presentation.base.BaseActivity
import com.m3sv.plainupnp.upnp.PlainUpnpAndroidService
import kotlin.LazyThreadSafetyMode.NONE


class MainActivity : BaseActivity(),
    NavController.OnDestinationChangedListener {

    lateinit var mainActivitySubComponent: MainActivitySubComponent

    private lateinit var binding: MainActivityBinding

    private lateinit var viewModel: MainViewModel

    private lateinit var volumeIndicator: VolumeIndicator

    private var bottomBarMenu = R.menu.bottom_app_bar_home_menu

    private val controlsFragment: ControlsFragment by lazy(NONE) {
        (supportFragmentManager.findFragmentById(R.id.bottom_nav_drawer) as ControlsFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val intent = Intent(this, PlainUpnpAndroidService::class.java).apply {
                action = PlainUpnpAndroidService.START_SERVICE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        volumeIndicator = VolumeIndicator(this)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = getViewModel()

        findNavController(R.id.nav_host_container).addOnDestinationChangedListener(this)

        observeState()
        setupBottomNavigationListener()
        requestReadStoragePermission()

        if (savedInstanceState != null)
            with(savedInstanceState) {
                restoreChevronState()
                restoreMenu()
            }

        animateBottomDrawChanges()
        binding.controlsContainer.setOnClickListener { controlsFragment.toggle() }
        setSupportActionBar(binding.bottomBar)
    }

    private fun observeState() {
        viewModel.volume.observe(this) { volume ->
            volumeIndicator.volume = volume
        }

        viewModel.shutdown.observe(this) {
            finishAndRemoveTask()
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (destination.id) {
            R.id.home_fragment -> setupBottomAppBarForHome()
            R.id.settings_fragment -> setupBottomAppBarForSettings()
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
                controlsFragment.close()
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

    private fun animateBottomDrawChanges() {
        with(controlsFragment) {
            addOnStateChangedAction(
                TriggerOnceStateAction(
                    this@MainActivity::animateChevronArrow
                )
            )
            addOnStateChangedAction(
                ChangeSettingsMenuStateAction(
                    this@MainActivity::replaceAppBarMenu
                )
            )
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
            clearSearchItemInput()
            replaceMenu(bottomBarMenu)
            setupSearchMenuItem(menu, false)
        }
    }

    private fun BottomAppBar.clearSearchItemInput() {
        menu.findItem(R.id.menu_search)?.let { item ->
            (item.actionView as SearchView).apply {
                setQuery("", true)
            }
        }
    }

    private val arrowUpAnimator by lazy(mode = NONE) {
        ObjectAnimator
            .ofFloat(binding.bottomAppBarChevron, ROTATION, 0f)
            .apply { duration = 200 }
    }

    private val arrowDownAnimator by lazy(mode = NONE) {
        ObjectAnimator
            .ofFloat(binding.bottomAppBarChevron, ROTATION, 180f)
            .apply { duration = 200 }
    }

    private fun animateChevronArrow(isHidden: Boolean) {
        val animator = if (isHidden) {
            arrowUpAnimator
        } else {
            arrowDownAnimator
        }

        animator.start()
    }

    private fun setupBottomNavigationListener() {
        NavigationUI.setupWithNavController(
            binding.bottomBar,
            findNavController(R.id.nav_host_container)
        )
    }

    private fun inject() {
        mainActivitySubComponent =
            (applicationContext as App).appComponent.mainSubcomponent().create()
        mainActivitySubComponent.inject(this)
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
