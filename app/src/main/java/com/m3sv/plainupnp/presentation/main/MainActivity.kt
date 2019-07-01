package com.m3sv.plainupnp.presentation.main

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.utils.enforce
import com.m3sv.plainupnp.common.utils.onItemSelectedListener
import com.m3sv.plainupnp.common.utils.onSeekBarChangeListener
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.RendererState
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.databinding.MainActivityBinding
import com.m3sv.plainupnp.presentation.base.ActivityConfig
import com.m3sv.plainupnp.presentation.base.BaseActivity
import com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter
import com.m3sv.plainupnp.upnp.LocalModel
import com.m3sv.plainupnp.upnp.RenderedItem
import timber.log.Timber


private val RendererState.icon: Int
    inline get() = when (state) {
        UpnpRendererState.State.STOP -> R.drawable.ic_play_arrow
        UpnpRendererState.State.PLAY -> R.drawable.ic_pause
        UpnpRendererState.State.PAUSE -> R.drawable.ic_play_arrow
        UpnpRendererState.State.INITIALIZING -> R.drawable.ic_play_arrow
    }

class MainActivity : BaseActivity<MainActivityBinding>() {

    override val activityConfig: ActivityConfig = ActivityConfig(R.layout.main_activity)

    private lateinit var navigator: MainActivityNavigator

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var rendererAdapter: SimpleArrayAdapter<DeviceDisplay>

    private lateinit var contentDirectoryAdapter: SimpleArrayAdapter<DeviceDisplay>

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private fun handleContentDirectories(contentDirectories: List<DeviceDisplay>) {
        contentDirectoryAdapter.setNewItems(contentDirectories)
    }

    private fun handleRenderers(renderers: List<DeviceDisplay>) {
        rendererAdapter.setNewItems(renderers)
    }

    private fun handleRendererState(rendererState: RendererState) {
        with(binding.controlsSheet) {
            with(progress) {
                isEnabled = rendererState.state != UpnpRendererState.State.STOP
                progress = rendererState.progress
            }

            play.setImageResource(rendererState.icon)
        }
    }

    private fun handleRenderedItem(item: RenderedItem) {
        with(item) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            Glide.with(this@MainActivity)
                    .load(uri)
                    .apply(requestOptions)
                    .into(binding.controlsSheet.art)

            binding.controlsSheet.title.text = title
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = MainActivityRouter(this)
        viewModel = getViewModel()

        with(binding) {
            vm = viewModel
            lifecycleOwner = this@MainActivity
        }

        rendererAdapter = SimpleArrayAdapter(this)

        contentDirectoryAdapter = SimpleArrayAdapter(this)

        setupBottomNavigation(binding.bottomNav)

        with(binding.controlsSheet) {
            bottomSheetBehavior = BottomSheetBehavior.from(container)

            progress.isEnabled = false

            next.setOnClickListener {
                viewModel.execute(MainCommand.NextClicked)
            }

            previous.setOnClickListener {
                viewModel.execute(MainCommand.PreviousClicked)
            }

            play.setOnClickListener {
                viewModel.execute(MainCommand.PlayClicked)
            }

            progress.setOnSeekBarChangeListener(onSeekBarChangeListener {
                viewModel.execute(MainCommand.MoveTo(it))
            })

            with(mainRendererDevicePicker) {
                adapter = rendererAdapter
                onItemSelectedListener = onItemSelectedListener { position ->
                    viewModel.execute(MainCommand.SelectRenderer(position))
                }
            }

            with(mainContentDevicePicker) {
                adapter = contentDirectoryAdapter
                onItemSelectedListener = onItemSelectedListener { position ->
                    viewModel.execute(MainCommand.SelectContentDirectory(position))
                }
            }
        }

        viewModel.state.nonNullObserve { state ->
            when (state) {
                is MainState.LaunchLocally -> launchLocally(state.model)
                is MainState.ContentDirectoriesDiscovered -> handleContentDirectories(state.devices)
                is MainState.RenderersDiscovered -> handleRenderers(state.devices)
                is MainState.RenderItem -> handleRenderedItem(state.item)
                is MainState.UpdateRendererState -> handleRendererState(state.rendererState)
                is MainState.Exit -> finishAndRemoveTask()
                is MainState.NavigateBack -> {
                }
            }.enforce
        }

        requestReadStoragePermission()
    }

    override fun onStart() {
        super.onStart()
        viewModel.execute(MainCommand.ResumeUpnp)
    }

    override fun onStop() {
        viewModel.execute(MainCommand.PauseUpnp)
        super.onStop()
    }

    private fun setupBottomNavigation(bottomNavigation: BottomNavigationView) {
        bottomNavigation.setOnNavigationItemSelectedListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

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

    private fun launchLocally(item: LocalModel) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.uri)).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
                setDataAndType(Uri.parse(item.uri), item.contentType)
            }

            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onBackPressed() {
        viewModel.execute(MainCommand.Navigate(Route.Back))
    }
}