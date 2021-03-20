package com.m3sv.plainupnp.presentation.main

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.ROTATION
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.TriggerOnceStateAction
import com.m3sv.plainupnp.common.util.doNothing
import com.m3sv.plainupnp.common.util.exhaustive
import com.m3sv.plainupnp.common.util.inputMethodManager
import com.m3sv.plainupnp.core.eventbus.events.ExitApplication
import com.m3sv.plainupnp.core.eventbus.subscribe
import com.m3sv.plainupnp.databinding.MainActivityBinding
import com.m3sv.plainupnp.presentation.home.HomeFragment
import com.m3sv.plainupnp.presentation.inappplayer.ImageFragment
import com.m3sv.plainupnp.presentation.inappplayer.PlayerFragment
import com.m3sv.plainupnp.presentation.main.controls.ControlsFragment
import com.m3sv.plainupnp.presentation.settings.SettingsFragment
import com.m3sv.plainupnp.upnp.folder.Folder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.properties.Delegates

private const val CHEVRON_ROTATION_ANGLE_KEY = "chevron_rotation_angle_key"
private const val IS_SEARCH_CONTAINER_VISIBLE = "is_search_container_visible_key"
private const val ARE_CONTROLS_VISIBLE = "are_controls_visible"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding

    private val viewModel: MainViewModel by viewModels()

    private val volumeIndicator: VolumeIndicator by lazy(NONE) { VolumeIndicator(this) }

    private val controlsFragment: ControlsFragment by lazy(NONE) {
        (supportFragmentManager.findFragmentById(R.id.bottom_nav_drawer) as ControlsFragment)
    }

    private var isConnectedToRenderer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateView()

        lifecycleScope.launchWhenCreated {
            viewModel.finishFlow.collect {
                finishAndRemoveTask()
            }
        }

        if (savedInstanceState != null) {
            Handler().post {
                with(savedInstanceState) {
                    restoreChevronState()
                    restoreSearchContainerVisibility()
                    restoreControlsVisibility()
                }
            }
        }

        observeState()
        animateBottomDrawChanges()

        with(binding) {
            binding.navigationStrip.scope = lifecycleScope
            setSupportActionBar(toolbar)
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.setDisplayShowTitleEnabled(false)
            }

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

    private fun replaceFragment(
        fragment: Fragment,
        tag: String? = null,
        addToBackStack: Boolean = false,
    ) {
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
        lifecycleScope.launchWhenCreated {
            subscribe<ExitApplication>().collect { finishAffinity() }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.volume.collect { volume: Int ->
                volumeIndicator.volume = volume
            }
        }

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
            .observe(this) { folders ->
                binding.navigationStrip.replaceItems(folders)

                val clickListener = folders
                    .firstOrNull()
                    ?.let { folder -> View.OnClickListener { navigateToFolder(folder) } }

                binding.navigateHome.setOnClickListener(clickListener)
            }


        viewModel.isConnectedToRenderer.asLiveData().observe(this) {
            isConnectedToRenderer = it != null
        }

        viewModel.navigation.observe(this) { navigationEvent ->
            navigationEvent.consume { route ->
                when (route) {
                    is MainRoute.Settings -> {
                        areControlsVisible = false
                        replaceFragment(SettingsFragment(), addToBackStack = true)
                    }
                    is MainRoute.Back -> {
                        supportFragmentManager.popBackStack(route.folder?.id, 0)
                        areControlsVisible = true
                    }
                    is MainRoute.ToFolder -> doNothing
                    is MainRoute.PreviewImage -> {
                        areControlsVisible = false
                        replaceFragment(ImageFragment.newInstance(route.url),
                            addToBackStack = true)
                    }
                    is MainRoute.PreviewVideo -> {
                        areControlsVisible = false
                        replaceFragment(PlayerFragment.newInstance(route.url),
                            addToBackStack = true)
                    }
                    is MainRoute.PreviewAudio -> {
                        areControlsVisible = false
                        replaceFragment(PlayerFragment.newInstance(route.url),
                            addToBackStack = true)
                    }
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
            android.R.id.home -> onBackPressed()
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
        super.onSaveInstanceState(outState)
        with(outState) {
            putFloat(CHEVRON_ROTATION_ANGLE_KEY, binding.bottomAppBarChevron.rotation)
            putBoolean(IS_SEARCH_CONTAINER_VISIBLE, binding.searchContainer.isVisible)
            putBoolean(ARE_CONTROLS_VISIBLE, areControlsVisible)
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
            0 -> finishAndRemoveTask()
            else -> viewModel.navigate(MainRoute.Back(null))
        }
    }
}
