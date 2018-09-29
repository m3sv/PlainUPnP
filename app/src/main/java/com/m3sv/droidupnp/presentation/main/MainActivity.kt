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
import android.widget.Toast
import com.bumptech.glide.Glide
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.databinding.MainActivityBinding
import com.m3sv.droidupnp.presentation.base.BaseActivity
import com.m3sv.droidupnp.presentation.settings.SettingsFragment
import com.m3sv.droidupnp.upnp.Directory
import com.m3sv.droidupnp.upnp.RenderedItem
import com.m3sv.droidupnp.upnp.UpnpManager
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.droidupnp.model.upnp.IRendererState
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
                navigateHome()
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

    private val renderersObserver = Observer<Set<DeviceDisplay>> {
        it?.run {
            Timber.d("Received new set of renderers: ${it.size}")
            rendererAdapter.run {
                clear()
                addAll(it.map { deviceDisplay -> deviceDisplay.device.displayString }.toList())
            }
        }
    }

    private val contentDirectoriesObserver = Observer<Set<DeviceDisplay>> {
        it?.run {
            Timber.d("Received new set of content directories: ${it.size}")
            with(contentDirectoryAdapter) {
                clear()
                addAll(it.map { deviceDisplay -> deviceDisplay.device.displayString }.toList())
            }
        }
    }

    private val rendererStateObserver = Observer<UpnpManager.RendererState> {
        it?.let {
            with(binding.controlsSheet) {
                progress.isEnabled = it.state != IRendererState.State.STOP
                progress.progress = it.progress
            }
        }
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private var currentDirectory: Directory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)

        with(binding) {
            vm = viewModel
            setLifecycleOwner(this@MainActivity)
        }

        setupBottomNavigation(binding.bottomNav)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.controlsSheet.container)

        binding.controlsSheet.progress.isEnabled = false

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, MainFragment.newInstance())
                .commit()
        }

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

        disposables += viewModel
            .selectedDirectoryObservable
            .subscribeBy(
                onNext = {
                    currentDirectory = it
                },
                onError = Timber::e
            )
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
        Glide.with(this@MainActivity).load(it?.first).into(binding.controlsSheet.art)
        binding.controlsSheet.title.text = it?.second
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
            resumeController()
        }
    }

    override fun onStop() {
        with(viewModel) {
            removeObservers()
            pauseController()
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

        if (supportFragmentManager.backStackEntryCount == 0 && currentDirectory is Directory.Home) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastBackClick < 500)
                finish()

            lastBackClick = currentTime
            Toast.makeText(this, R.string.to_exit, Toast.LENGTH_SHORT).show()
        } else if (currentDirectory != null) {
            viewModel.pop()
        } else {
            finish()
        }
    }

    companion object {
        private const val REQUEST_READ_EXT_STORAGE = 12345
    }
}