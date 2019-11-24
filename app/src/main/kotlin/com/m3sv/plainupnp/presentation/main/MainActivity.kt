package com.m3sv.plainupnp.presentation.main

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.utils.enforce
import com.m3sv.plainupnp.data.upnp.UpnpItemType
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.databinding.MainActivityBinding
import com.m3sv.plainupnp.presentation.base.ActivityConfig
import com.m3sv.plainupnp.presentation.base.BaseActivity


private val UpnpRendererState.icon: Int
    inline get() = when (state) {
        UpnpRendererState.State.STOP -> R.drawable.ic_play_arrow
        UpnpRendererState.State.PLAY -> R.drawable.ic_pause
        UpnpRendererState.State.PAUSE -> R.drawable.ic_play_arrow
        UpnpRendererState.State.INITIALIZING -> R.drawable.ic_play_arrow
        UpnpRendererState.State.FINISHED -> R.drawable.ic_play_arrow
    }

class MainActivity : BaseActivity<MainActivityBinding>() {

    override val activityConfig: ActivityConfig = ActivityConfig(R.layout.main_activity)

    private lateinit var navigator: MainActivityNavigator

    private lateinit var viewModel: MainViewModel

    private lateinit var controlsSheetDelegate: ControlsSheetDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()

        initRouter()
        initControlsSheetDelegate()
        observeState()
        setupBottomNavigationListener()
        requestReadStoragePermission()

        if (savedInstanceState != null) {
            restoreControlsSheetState(savedInstanceState)
        } else {
            startUpnpService()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveControlsSheetState(outState)
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

    override fun onBackPressed() {
        viewModel.execute(MainIntention.Navigate(Route.Back))
    }

    private fun initRouter() {
        navigator = MainActivityRouter(this)
    }

    private fun initControlsSheetDelegate() {
        controlsSheetDelegate = ControlsSheetDelegate(binding.controlsSheet, viewModel::execute)
    }

    private fun restoreControlsSheetState(bundle: Bundle) {
        controlsSheetDelegate.onRestoreInstanceState(bundle)
    }

    private fun startUpnpService() {
        viewModel.execute(MainIntention.StartUpnpService)
    }

    private fun setupBottomNavigationListener() {
        binding.bottomNav.setOnNavigationItemSelectedListener {
            controlsSheetDelegate.collapseSheet()

            if (it.itemId != binding.bottomNav.selectedItemId)
                when (it.itemId) {
                    R.id.nav_home -> {
                        navigator.navigateToMain()
                        true
                    }

                    R.id.nav_settings -> {
                        navigator.navigateToSettings()
                        true
                    }

                    else -> false
                }
            else false
        }
    }

    private fun observeState() {
        viewModel.state.nonNullObserve { state ->
            when (state) {
                is MainState.Render -> {
                    with(controlsSheetDelegate) {
                        updateContentDirectories(state.contentDirectories)
                        updateRenderers(state.renderers)
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
        with(binding.controlsSheet) {
            with(progress) {
                isEnabled = rendererState.state == UpnpRendererState.State.PLAY
                progress = rendererState.elapsedPercent
            }

            play.setImageResource(rendererState.icon)
        }

        with(binding.controlsSheet.art) {
            var thumb: Any? = when (rendererState.type) {
                UpnpItemType.AUDIO -> R.drawable.ic_music
                else -> rendererState.uri
            }

            thumb = thumb ?: R.drawable.ic_launcher_no_shadow

            Glide.with(this).load(thumb).into(this)
        }

        binding.controlsSheet.title.text = rendererState.title
    }

    private fun saveControlsSheetState(outState: Bundle) {
        controlsSheetDelegate.onSaveInstanceState(outState)
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_exit_title))
            .setMessage(getString(R.string.dialog_exit_body))
            .setPositiveButton(getString(R.string.exit)) { _, _ ->
                // todo clear latest state when finish
                finishAndRemoveTask()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .show()
    }

}
