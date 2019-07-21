package com.m3sv.plainupnp.presentation.main

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.Consumable
import com.m3sv.plainupnp.common.utils.enforce
import com.m3sv.plainupnp.common.utils.onItemSelectedListener
import com.m3sv.plainupnp.common.utils.onSeekBarChangeListener
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.RendererState
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.databinding.MainActivityBinding
import com.m3sv.plainupnp.presentation.base.*
import com.m3sv.plainupnp.upnp.LocalModel
import com.m3sv.plainupnp.upnp.RenderedItem
import timber.log.Timber


private val RendererState.icon: Int
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

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var rendererAdapter: SimpleArrayAdapter<Renderer>

    private lateinit var contentDirectoryAdapter: SimpleArrayAdapter<ContentDirectory>

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private fun handleContentDirectories(contentDirectories: List<DeviceDisplay>) {
        Timber.d("New number of content directories: ${contentDirectories.size}")
        contentDirectoryAdapter.setNewItems(contentDirectories.map { ContentDirectory(it.device.friendlyName) })
    }

    private fun handleRenderers(renderers: List<DeviceDisplay>) {
        Timber.d("New number of renderers: ${renderers.size}")
        rendererAdapter.setNewItems(renderers.map { Renderer(it.device.friendlyName) })
    }

    private fun handleRendererState(rendererState: RendererState) {
        with(binding.controlsSheet) {
            with(progress) {
                isEnabled = rendererState.isControlEnabled
                progress = rendererState.progress
            }

            play.setImageResource(rendererState.icon)
        }
    }

    private fun handleRenderedItem(item: RenderedItem) {
        Timber.v("Handle rendered item: ${item.uri}")
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
        Timber.d("onCreate!")
        navigator = MainActivityRouter(this)
        viewModel = getViewModel()

        with(binding) {
            vm = viewModel
            lifecycleOwner = this@MainActivity
        }

        rendererAdapter = SimpleArrayAdapter.init(this)

        contentDirectoryAdapter = SimpleArrayAdapter.init(this)

        savedInstanceState?.let {
            rendererAdapter.onRestoreInstanceState(it)
            contentDirectoryAdapter.onRestoreInstanceState(it)
        }

        setupBottomNavigation(binding.bottomNav)

        with(binding.controlsSheet) {
            bottomSheetBehavior = BottomSheetBehavior.from(container)
            progress.isEnabled = false

            next.setOnClickListener {
                viewModel.execute(MainIntention.NextClick)
            }

            previous.setOnClickListener {
                viewModel.execute(MainIntention.PreviousClick)
            }

            play.setOnClickListener {
                viewModel.execute(MainIntention.PlayClick)
            }

            progress.setOnSeekBarChangeListener(onSeekBarChangeListener {
                viewModel.execute(MainIntention.MoveTo(it))
            })

            with(mainRendererDevicePicker) {
                adapter = rendererAdapter
                onItemSelectedListener = onItemSelectedListener { position ->
                    viewModel.execute(MainIntention.SelectRenderer(position))
                }
            }

            with(mainContentDevicePicker) {
                adapter = contentDirectoryAdapter
                onItemSelectedListener = onItemSelectedListener { position ->
                    viewModel.execute(MainIntention.SelectContentDirectory(position))
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
        viewModel.execute(MainIntention.ResumeUpnp)
    }

    override fun onStop() {
        viewModel.execute(MainIntention.PauseUpnp)
        super.onStop()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                viewModel.execute(MainIntention.RaiseVolume)
                true
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                viewModel.execute(MainIntention.LowerVolume)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        rendererAdapter.onSaveInstanceState(outState)
        contentDirectoryAdapter.onSaveInstanceState(outState)
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

    private fun launchLocally(item: Consumable<LocalModel?>) {
        item.consume()?.let {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.uri)).apply {
                    flags = FLAG_ACTIVITY_NEW_TASK
                    setDataAndType(Uri.parse(it.uri), it.contentType)
                }

                startActivity(intent)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun onBackPressed() {
        viewModel.execute(MainIntention.Navigate(Route.Back))
    }
}