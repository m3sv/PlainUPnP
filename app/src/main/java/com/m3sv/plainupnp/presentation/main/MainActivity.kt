package com.m3sv.plainupnp.presentation.main

import android.Manifest
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.AdapterView
import android.widget.SeekBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding2.view.RxView
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.Directory
import com.m3sv.plainupnp.data.upnp.RendererState
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.databinding.MainActivityBinding
import com.m3sv.plainupnp.presentation.base.BaseActivity
import com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter
import com.m3sv.plainupnp.presentation.settings.SettingsFragment
import com.m3sv.plainupnp.upnp.LaunchLocally
import com.m3sv.plainupnp.upnp.RenderedItem
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber


class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var binding: MainActivityBinding

    private lateinit var rendererAdapter: SimpleArrayAdapter<DeviceDisplay>

    private lateinit var contentDirectoryAdapter: SimpleArrayAdapter<DeviceDisplay>

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private val contentDirectorySpinnerClickListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            Timber.d("Selected item: $position")
            viewModel.selectContentDirectory(contentDirectoryAdapter.getItem(position)?.device)
        }
    }

    private val rendererSpinnerClickListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            Timber.d("Selected renderer: $position")
            viewModel.selectRenderer(rendererAdapter.getItem(position)?.device)
        }
    }

    private fun handleContentDirectories(contentDirectories: Set<DeviceDisplay>) {
        Timber.d("Received new set of content directories: ${contentDirectories.size}")
        contentDirectoryAdapter.setNewItems(contentDirectories.toList())
    }

    private fun handleRenderers(renderers: Set<DeviceDisplay>) {
        Timber.d("Received new set of renderers: ${renderers.size}")
        rendererAdapter.setNewItems(renderers.toList())
    }

    private fun handleRendererState(rendererState: RendererState) {
        with(binding.controlsSheet) {
            progress.isEnabled = rendererState.state != UpnpRendererState.State.STOP
            progress.progress = rendererState.progress

            disposables += when (rendererState.state) {
                UpnpRendererState.State.STOP -> {
                    play.setImageResource(R.drawable.ic_play_arrow)
                    RxView.clicks(play).subscribeBy(onNext = {
                        viewModel.resumePlayback()
                    }, onError = Timber::e)
                }

                UpnpRendererState.State.PLAY -> {
                    play.setImageResource(R.drawable.ic_pause)
                    RxView.clicks(play).subscribeBy(onNext = {
                        viewModel.pausePlayback()
                    }, onError = Timber::e)
                }

                UpnpRendererState.State.PAUSE -> {
                    play.setImageResource(R.drawable.ic_play_arrow)
                    RxView.clicks(play).subscribeBy(onNext = {
                        viewModel.resumePlayback()
                    }, onError = Timber::e)
                }

                UpnpRendererState.State.INITIALIZING -> {
                    play.setImageResource(R.drawable.ic_play_arrow)
                    RxView.clicks(play).subscribeBy(onNext = {
                        viewModel.resumePlayback()
                    }, onError = Timber::e)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)

        with(binding) {
            vm = viewModel
            setLifecycleOwner(this@MainActivity)
        }

        setupBottomNavigation(binding.bottomNav)

        initBottomSheet()

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, MainFragment.newInstance())
                .commit()


            viewModel.resumeUpnpController()
        }

        disposables += RxView.clicks(binding.controlsSheet.next).subscribeBy(onNext = {
            viewModel.playNext()
        }, onError = Timber::e)

        disposables += RxView.clicks(binding.controlsSheet.previous).subscribeBy(onComplete = {
            viewModel.playPrevious()
        }, onError = Timber::e)

        disposables += viewModel.launchLocally.subscribeBy(
            onNext = ::launchLocally,
            onError = Timber::e
        )

        with(viewModel) {
            renderers.nonNullObserve(::handleRenderers)
            contentDirectories.nonNullObserve(::handleContentDirectories)
            rendererState.nonNullObserve(::handleRendererState)
            renderedItem.nonNullObserve(::handleRenderedItem)
        }

        initPickers()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXT_STORAGE
                )
            }
        }

        binding.controlsSheet.progress.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    Timber.i("From user")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                Timber.i("onStartTrackingTouch")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Timber.i("onStopTrackingTouch")
                viewModel.moveTo(seekBar.progress, seekBar.max)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        with(viewModel) {
            addObservers()
            resumeUpnp()
            resumeRendererUpdate()
        }
    }

    override fun onStop() {
        with(viewModel) {
            pauseRendererUpdate()
            removeObservers()
            pauseUpnp()
        }
        super.onStop()
    }

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.controlsSheet.container)
        binding.controlsSheet.progress.isEnabled = false
    }

    private fun setupBottomNavigation(bottomNavigation: BottomNavigationView) {
        bottomNavigation.setOnNavigationItemSelectedListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            if (it.itemId != binding.bottomNav.selectedItemId)
                when (it.itemId) {
                    R.id.nav_home -> {
                        navigateToMain()
                        true
                    }

                    R.id.nav_settings -> {
                        navigateToSettings()
                        true
                    }

                    else -> false
                }
            else false
        }
    }

    private fun handleRenderedItem(item: RenderedItem) {
        with(item) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            Glide.with(this@MainActivity)
                .load(first)
                .apply(third)
                .into(binding.controlsSheet.art)

            binding.controlsSheet.title.text = second
        }
    }

    private fun launchLocally(item: LaunchLocally?) {
        item?.let {
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
    }

    private fun initPickers() {
        with(binding.controlsSheet) {
            rendererAdapter = SimpleArrayAdapter(this@MainActivity)
            with(mainRendererDevicePicker) {
                adapter = rendererAdapter
                onItemSelectedListener = rendererSpinnerClickListener
            }

            contentDirectoryAdapter = SimpleArrayAdapter(this@MainActivity)
            with(mainContentDevicePicker) {
                adapter = contentDirectoryAdapter
                onItemSelectedListener = contentDirectorySpinnerClickListener
            }
        }
    }

    private fun navigateToMain() {
        supportFragmentManager.popBackStackImmediate()
    }

    private fun navigateToSettings() {
        val tag = SettingsFragment.TAG
        val fragment = supportFragmentManager.findFragmentByTag(tag)

        if (fragment == null) {
            navigateTo(SettingsFragment.newInstance(), tag, true)
        } else {
            navigateTo(fragment, tag, true)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            binding.bottomNav.selectedItemId = R.id.nav_home
            return
        }

        if (supportFragmentManager.backStackEntryCount == 0 && viewModel.currentDirectory is Directory.Home) {
            doubleTapExit()
        } else if (viewModel.currentDirectory != null) {
            viewModel.browsePrevious()
        } else {
            finish()
        }
    }

    private fun doubleTapExit() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastBackClick < 500)
            finish()

        lastBackClick = currentTime
        Toast.makeText(this, R.string.to_exit, Toast.LENGTH_SHORT).show()
    }

    private var lastBackClick = System.currentTimeMillis()

    companion object {
        private const val REQUEST_READ_EXT_STORAGE = 12345
    }
}