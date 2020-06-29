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
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFade
import com.m3sv.plainupnp.App
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.TriggerOnceStateAction
import com.m3sv.plainupnp.databinding.MainActivityBinding
import com.m3sv.plainupnp.presentation.base.BaseActivity
import com.m3sv.plainupnp.presentation.main.di.MainActivitySubComponent
import com.m3sv.plainupnp.upnp.PlainUpnpAndroidService
import kotlin.LazyThreadSafetyMode.NONE

private const val CHEVRON_ROTATION_ANGLE_KEY = "chevron_rotation_angle_key"
private const val OPTIONS_MENU_KEY = "options_menu_key"
private const val IS_SEARCH_CONTAINER_VISIBLE = "is_search_container_visible_key"

class MainActivity : BaseActivity(), NavController.OnDestinationChangedListener {

    lateinit var mainActivitySubComponent: MainActivitySubComponent

    private lateinit var binding: MainActivityBinding

    private lateinit var viewModel: MainViewModel

    private lateinit var volumeIndicator: VolumeIndicator

    private lateinit var inputMethodManager: InputMethodManager

    private var bottomBarMenu = R.menu.bottom_app_bar_home_menu

    private val controlsFragment: ControlsFragment by lazy(NONE) {
        (supportFragmentManager.findFragmentById(R.id.bottom_nav_drawer) as ControlsFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

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
        viewModel
            .errors
            .observe(this) { consumable ->
                consumable.consume { value ->
                    MaterialAlertDialogBuilder(this)
                        .setMessage(value)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
        findNavController(R.id.nav_host_container).addOnDestinationChangedListener(this)

        observeState()
        setupBottomNavigationListener()
        requestReadStoragePermission()

        if (savedInstanceState != null)
            with(savedInstanceState) {
                restoreChevronState()
                restoreMenu()
                restoreSearchContainerVisibility()
            }

        animateBottomDrawChanges()

        binding.controlsContainer.setOnClickListener { view ->
            hideSearchContainer(false)
            view.postDelayed({ controlsFragment.toggle() }, 50)
        }
        setSupportActionBar(binding.bottomBar)

        binding.searchClose.setOnClickListener {
            hideSearchContainer(true)
        }

        binding.searchInput.addTextChangedListener { text ->
            if (text != null) viewModel.filterText(text.toString())
        }
    }

    private fun hideSearchContainer(animate: Boolean) {
        if (animate) {
            val animationDuration = 150L
            animateVisibilityChange(animationDuration)
        }

        with(binding) {
            val currentFocus = currentFocus
            if (inputMethodManager.isActive(searchInput) && currentFocus != null) {
                inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            }
            searchInput.clearFocus()
            searchContainer.visibility = View.INVISIBLE
            searchInput.setText("")
        }
    }

    private fun observeState() {
        viewModel.volume.observe(this) { volume: Int ->
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> showSettings()
            R.id.menu_search -> showSearch()
        }
        return true
    }

    private fun showSearch() {
        controlsFragment.close()
        val animationDuration = 150L
        animateVisibilityChange(animationDuration)
        with(binding.searchContainer) {
            isVisible = true
            postDelayed({
                if (binding.searchInput.requestFocus()) {
                    inputMethodManager.showSoftInput(binding.searchInput, 0)
                }
            }, animationDuration)
        }
    }

    private fun animateVisibilityChange(animationDuration: Long) {
        val materialFade = MaterialFade().apply {
            duration = animationDuration
        }
        TransitionManager.beginDelayedTransition(binding.root, materialFade)
    }

    private fun showSettings() {
        hideSearchContainer(false)
        controlsFragment.close()
        findNavController(R.id.nav_host_container).navigate(R.id.action_mainFragment_to_settingsFragment)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat(CHEVRON_ROTATION_ANGLE_KEY, binding.bottomAppBarChevron.rotation)
        outState.putInt(OPTIONS_MENU_KEY, bottomBarMenu)
        outState.putBoolean(IS_SEARCH_CONTAINER_VISIBLE, binding.searchContainer.isVisible)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
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

    private fun animateBottomDrawChanges() {
        with(controlsFragment) {
            addOnStateChangedAction(TriggerOnceStateAction(this@MainActivity::animateChevronArrow))
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

    private fun Bundle.restoreSearchContainerVisibility() {
        val isSearchContainerVisible = getBoolean(IS_SEARCH_CONTAINER_VISIBLE, false)
        binding.searchContainer.visibility =
            if (isSearchContainerVisible) View.VISIBLE else View.INVISIBLE
    }
}
