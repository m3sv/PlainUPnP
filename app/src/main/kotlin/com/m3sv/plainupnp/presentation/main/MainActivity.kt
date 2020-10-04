package com.m3sv.plainupnp.presentation.main

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.ROTATION
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.m3sv.plainupnp.App
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.TriggerOnceStateAction
import com.m3sv.plainupnp.common.util.doNothing
import com.m3sv.plainupnp.common.util.exhaustive
import com.m3sv.plainupnp.common.util.inputMethodManager
import com.m3sv.plainupnp.databinding.MainActivityBinding
import com.m3sv.plainupnp.presentation.base.BaseActivity
import com.m3sv.plainupnp.presentation.home.HomeFragment
import com.m3sv.plainupnp.presentation.inappplayer.ImageFragment
import com.m3sv.plainupnp.presentation.main.controls.ControlsFragment
import com.m3sv.plainupnp.presentation.main.di.MainActivitySubComponent
import com.m3sv.plainupnp.presentation.onboarding.OnboardingFragment
import com.m3sv.plainupnp.presentation.settings.SettingsFragment
import com.m3sv.plainupnp.upnp.PlainUpnpAndroidService
import com.m3sv.plainupnp.upnp.folder.Folder
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.properties.Delegates

private const val CHEVRON_ROTATION_ANGLE_KEY = "chevron_rotation_angle_key"
private const val IS_SEARCH_CONTAINER_VISIBLE = "is_search_container_visible_key"
private const val ARE_CONTROLS_VISIBLE = "are_controls_visible"

class MainActivity : BaseActivity() {

    lateinit var mainActivitySubComponent: MainActivitySubComponent

    private lateinit var binding: MainActivityBinding

    private lateinit var viewModel: MainViewModel

    private val volumeIndicator: VolumeIndicator by lazy(NONE) { VolumeIndicator(this) }

    private val controlsFragment: ControlsFragment by lazy(NONE) {
        (supportFragmentManager.findFragmentById(R.id.bottom_nav_drawer) as ControlsFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        inflateView()

        if (savedInstanceState != null) {
            Handler().post {
                with(savedInstanceState) {
                    restoreChevronState()
                    restoreSearchContainerVisibility()
                    restoreControlsVisibility()
                }
            }
        } else {
            val intent = Intent(this, PlainUpnpAndroidService::class.java).apply {
                action = PlainUpnpAndroidService.START_SERVICE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

            addFragment(OnboardingFragment())
        }

        requestReadStoragePermission()

        viewModel = getViewModel()

        observeState()
        animateBottomDrawChanges()

        with(binding) {
            setSupportActionBar(bottomBar)

            controlsContainer.setOnClickListener { view ->
                hideSearchContainer()
                view.postDelayed({ controlsFragment.toggle() }, 50)
            }

            searchClose.setOnClickListener {
                hideSearchContainer()
            }

            searchInput.addTextChangedListener { if (it != null) viewModel.filterText(it.toString()) }

            lifecycleScope.launch {
                navigationStrip.clickFlow.collect { folder -> navigateToFolder(folder) }
            }
        }
    }

    private fun inflateView() {
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun addFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.nav_host_container, fragment)
            .commit()
    }

    private fun replaceFragment(fragment: Fragment, tag: String, addToBackStack: Boolean = false) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_container, fragment, tag)
            .apply {
                if (addToBackStack) {
                    addToBackStack(tag)
                }
            }.commit()
    }

    private fun navigateToFolder(folder: Folder) {
        when (folder) {
            is Folder.Root -> viewModel.navigate(MainRoute.ToFolder(folder))
            is Folder.SubFolder -> viewModel.navigate(MainRoute.Back(folder))
        }
    }

    private fun hideSearchContainer() {
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
        viewModel
            .finishFlow
            .observe(this) {
                finishAndRemoveTask()
            }

        viewModel
            .volume
            .observe(this) { volume: Int -> volumeIndicator.volume = volume }

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

        viewModel
            .changeFolder
            .observe(this) { changeEvent ->
                areControlsVisible = true

                changeEvent.consume { folderType ->
                    when (folderType) {
                        is Folder.Root -> {
                            supportFragmentManager.popBackStack(
                                null,
                                POP_BACK_STACK_INCLUSIVE
                            )
                            replaceFragment(HomeFragment(), folderType.id)
                        }

                        is Folder.SubFolder -> replaceFragment(
                            HomeFragment(),
                            folderType.id,
                            true
                        )
                    }
                }
            }

        viewModel
            .navigationStrip
            .observe(this) { folders -> binding.navigationStrip.replaceItems(folders) }

        viewModel.navigation.observe(this) { navigationEvent ->
            navigationEvent.consume { route ->
                when (route) {
                    is MainRoute.Settings -> {
                        areControlsVisible = false
                        replaceFragment(SettingsFragment(), "settings", true)
                    }
                    is MainRoute.Back -> {
                        supportFragmentManager.popBackStack(route.folder?.id, 0)
                        areControlsVisible = true
                    }
                    is MainRoute.ToFolder -> doNothing
                    is MainRoute.PreviewImage -> {
                        areControlsVisible = false
                        replaceFragment(ImageFragment.newInstance(route.url),
                            "",
                            true)
                    }
                    is MainRoute.PreviewVideo -> TODO()
                    is MainRoute.PreviewAudio -> TODO()
                    is MainRoute.Initial -> doNothing
                }.exhaustive
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_DEL -> {
            handleBackPressed()
            true
        }
        else -> super.onKeyUp(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottom_app_bar_home_menu, menu)
        return true
    }

    private var areControlsVisible: Boolean by Delegates.observable(true) { _, _, visible ->
        if (visible) {
            with(binding) {
                bottomBar.performShow()
                binding.motionContainer.transitionToStart()
            }
        } else {
            hideSearchContainer()
            with(binding) {
                controlsFragment.close()
                bottomBar.performHide()
                binding.motionContainer.transitionToEnd()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> viewModel.navigate(MainRoute.Settings)
            R.id.menu_search -> showSearch()
        }
        return true
    }

    private fun showSearch() {
        controlsFragment.close()
        val animationDuration = 150L
        with(binding.searchContainer) {
            isVisible = true
            postDelayed({
                if (binding.searchInput.requestFocus()) {
                    inputMethodManager.showSoftInput(binding.searchInput, 0)
                }
            }, animationDuration)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        with(outState) {
            putFloat(CHEVRON_ROTATION_ANGLE_KEY, binding.bottomAppBarChevron.rotation)
            putBoolean(IS_SEARCH_CONTAINER_VISIBLE, binding.searchContainer.isVisible)
            putBoolean(ARE_CONTROLS_VISIBLE, areControlsVisible)
        }
        super.onSaveInstanceState(outState)
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
        controlsFragment.addOnStateChangedAction(TriggerOnceStateAction(this@MainActivity::animateChevronArrow))
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

    private fun inject() {
        mainActivitySubComponent = (applicationContext as App)
            .appComponent
            .mainSubcomponent()
            .create()
            .also { component -> component.inject(this) }
    }

    private fun Bundle.restoreChevronState() {
        binding.bottomAppBarChevron.rotation = getFloat(CHEVRON_ROTATION_ANGLE_KEY, 0f)
    }

    private fun Bundle.restoreSearchContainerVisibility() {
        val isSearchContainerVisible = getBoolean(IS_SEARCH_CONTAINER_VISIBLE, false)
        binding
            .searchContainer
            .visibility = if (isSearchContainerVisible) View.VISIBLE else View.INVISIBLE
    }

    private fun Bundle.restoreControlsVisibility() {
        areControlsVisible = getBoolean(ARE_CONTROLS_VISIBLE, false)
    }

    override fun onBackPressed() {
        handleBackPressed()
    }

    private fun handleBackPressed() {
        when (supportFragmentManager.backStackEntryCount) {
            0 -> showExitConfirmationDialog()
            else -> viewModel.navigate(MainRoute.Back(null))
        }
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_exit_title))
            .setMessage(getString(R.string.dialog_exit_body))
            .setPositiveButton(getString(R.string.exit)) { _, _ -> finishAndRemoveTask() }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .show()
    }
}
