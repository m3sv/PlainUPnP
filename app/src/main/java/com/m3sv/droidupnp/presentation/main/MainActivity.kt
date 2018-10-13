package com.m3sv.droidupnp.presentation.main

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding2.view.RxView
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.data.Directory
import com.m3sv.droidupnp.data.RendererState
import com.m3sv.droidupnp.databinding.MainActivityBinding
import com.m3sv.droidupnp.presentation.base.BaseActivity
import com.m3sv.droidupnp.presentation.settings.SettingsFragment
import com.m3sv.droidupnp.upnp.RenderedItem
import com.m3sv.droidupnp.upnp.DefaultUpnpManager
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import com.m3sv.droidupnp.data.UpnpRendererState
import org.droidupnp.view.DeviceDisplay
import timber.log.Timber


class MainActivity : BaseActivity() {

    lateinit var viewModel: MainActivityViewModel

    private lateinit var binding: MainActivityBinding

    private lateinit var rendererAdapter: ArrayAdapter<String>

    private lateinit var contentDirectoryAdapter: ArrayAdapter<String>

    private val contentDirectorySpinnerClickListener = object : AdapterView.OnItemSelectedListener {

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            with(viewModel) {
                Timber.d("Selected item: $position")
                selectContentDirectory(contentDirectoriesObservable.value?.toList()?.get(position)?.device)
                browseHome()
            }
        }
    }

    private val rendererSpinnerClickListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            with(viewModel) {
                Timber.d("Selected renderer: $position")
                selectRenderer(renderersObservable.value?.toList()?.get(position)?.device)
            }
        }
    }

    private val renderersObserver = Observer<Set<DeviceDisplay>>(::handleRenderers)

    private val contentDirectoriesObserver =
        Observer<Set<DeviceDisplay>>(::handleContentDirectories)

    private val rendererStateObserver = Observer<RendererState>(::handleRendererState)

    private fun handleContentDirectories(it: Set<DeviceDisplay>?) {
        it?.let {
            Timber.d("Received new set of content directories: ${it.size}")
            with(contentDirectoryAdapter) {
                clear()
                addAll(it.asSequence().map { deviceDisplay -> deviceDisplay.device.displayString }.toList())
            }
        }
    }

    private fun handleRendererState(it: RendererState?) {
        it?.let {
            with(binding.controlsSheet) {
                progress.isEnabled = it.state != UpnpRendererState.State.STOP
                progress.progress = it.progress

                disposables += when (it.state) {
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
                }
            }
        }
    }

    private fun handleRenderers(it: Set<DeviceDisplay>?) {
        it?.run {
            Timber.d("Received new set of renderers: ${it.size}")
            rendererAdapter.run {
                clear()
                addAll(it.map { deviceDisplay -> deviceDisplay.device.displayString }.toList())
            }
        }
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>


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
        }

        disposables += RxView.clicks(binding.controlsSheet.next).subscribeBy(onNext = {
            viewModel.playNext()
        }, onError = Timber::e)

        disposables += RxView.clicks(binding.controlsSheet.previous).subscribeBy(onNext = {
            viewModel.playPrevious()
        }, onError = Timber::e)

        initLiveData()
        setupPickers()

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

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.controlsSheet.container)

        binding.controlsSheet.progress.isEnabled = false
    }

    private fun setupBottomNavigation(bottomNavigation: BottomNavigationView) {
        bottomNavigation.setOnNavigationItemSelectedListener {
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

    private val renderedItemObserver = Observer<RenderedItem> {
        it?.run {
            Glide.with(this@MainActivity)
                .load(first)
                .apply(third)
                .into(binding.controlsSheet.art)
            binding.controlsSheet.title.text = second
        }
    }

    private fun initLiveData() {
        with(viewModel) {
            renderersObservable.observe(renderersObserver)
            contentDirectoriesObservable.observe(contentDirectoriesObserver)
            rendererState.observe(rendererStateObserver)
            renderedItem.observe(renderedItemObserver)
        }
    }

    private fun setupPickers() {
        with(binding.controlsSheet) {
            rendererAdapter =
                    ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1)
                        .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            with(mainRendererDevicePicker) {
                adapter = rendererAdapter
                onItemSelectedListener = rendererSpinnerClickListener
            }

            contentDirectoryAdapter =
                    ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1)
                        .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            with(mainContentDevicePicker) {
                adapter = contentDirectoryAdapter
                onItemSelectedListener = contentDirectorySpinnerClickListener
            }
        }
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

    private var lastBackClick = System.currentTimeMillis()

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            binding.bottomNav.selectedItemId = R.id.nav_home
            return
        }

        if (supportFragmentManager.backStackEntryCount == 0 && viewModel.currentDirectory is Directory.Home) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastBackClick < 500)
                finish()

            lastBackClick = currentTime
            Toast.makeText(this, R.string.to_exit, Toast.LENGTH_SHORT).show()
        } else if (viewModel.currentDirectory != null) {
            viewModel.browsePrevious()
        } else {
            finish()
        }
    }

    companion object {
        private const val REQUEST_READ_EXT_STORAGE = 12345
    }
}