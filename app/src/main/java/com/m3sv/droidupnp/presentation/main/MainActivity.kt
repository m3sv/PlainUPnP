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
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.databinding.MainActivityBinding
import com.m3sv.droidupnp.presentation.base.BaseActivity
import com.m3sv.droidupnp.presentation.settings.SettingsFragment
import org.droidupnp.view.DeviceDisplay
import timber.log.Timber


class MainActivity : BaseActivity() {

    lateinit var viewModel: MainActivityViewModel

    private lateinit var binding: MainActivityBinding

    private lateinit var rendererAdapter: ArrayAdapter<String>

    private lateinit var contentDirectoryAdapter: ArrayAdapter<String>

    private val contentDirectoryClickListener = object : AdapterView.OnItemSelectedListener {

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            with(viewModel) {
                selectDevice(contentDirectoriesObservable.value?.toList()?.get(position)?.device)
                navigateHome()
            }
        }
    }

    private val renderersObserver = Observer<Set<DeviceDisplay>> {
        it?.run {
            Timber.d("Received new set of renderers: ${it.size}")
            rendererAdapter.run {
                clear()
                addAll(it.map { deviceDisplay -> deviceDisplay.device.displayString }.toList())
                notifyDataSetChanged()
            }
        }
    }

    private val contentDirectoriesObserver = Observer<Set<DeviceDisplay>> {
        it?.run {
            Timber.d("Received new set of content directories: ${it.size}")
            with(contentDirectoryAdapter) {
                clear()
                addAll(it.map { deviceDisplay -> deviceDisplay.device.displayString }.toList())
                notifyDataSetChanged()
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

        bottomSheetBehavior = BottomSheetBehavior.from(binding.controlsSheet.container).also {
            it.isHideable = true
            it.state = BottomSheetBehavior.STATE_HIDDEN
        }

        restoreFragmentState(savedInstanceState)
        initObservers()
        setupPickers()
        setupBottomNavigation(binding.bottomNav)

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

    private fun initObservers() {
        with(viewModel) {
            renderersObservable.observe(this@MainActivity, renderersObserver)
            contentDirectoriesObservable.observe(this@MainActivity, contentDirectoriesObserver)
        }
    }

    private fun setupPickers() {
        binding.controlsSheet.run {
            rendererAdapter =
                    ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1)
                        .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            mainRendererDevicePicker.adapter = rendererAdapter

            contentDirectoryAdapter =
                    ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1)
                        .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            mainContentDevicePicker.adapter = contentDirectoryAdapter
            mainContentDevicePicker.onItemSelectedListener = contentDirectoryClickListener
        }
    }

    override fun onStart() {
        super.onStart()
        with(viewModel) {
            resetDevices()
            resumeController()
            addObservers()
        }
    }

    override fun onStop() {
        with(viewModel) {
            removeObservers()
            pauseController()
        }
        super.onStop()
    }

    private fun restoreFragmentState(savedInstanceState: Bundle?) {
        Timber.d("Last fragment tag: ${viewModel.lastFragmentTag}")

        if (savedInstanceState == null) {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        supportFragmentManager.popBackStackImmediate()

        val fragment = supportFragmentManager.findFragmentByTag(MainFragment.TAG)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isHideable = false
        if (fragment == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, MainFragment.newInstance(), MainFragment.TAG)
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment, MainFragment.TAG)
                .commit()
        }
    }

    private fun navigateToSettings() {
        val fragment = supportFragmentManager.findFragmentByTag(SettingsFragment.TAG)

        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        if (fragment == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, SettingsFragment.newInstance(), SettingsFragment.TAG)
                .addToBackStack(null)
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment, SettingsFragment.TAG)
                .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("LAST_FRAGMENT_TAG", viewModel.lastFragmentTag)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            binding.bottomNav.selectedItemId = R.id.nav_home
            return
        }

        if (supportFragmentManager.backStackEntryCount == 0)
            super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
    }

    companion object {
        private const val REQUEST_READ_EXT_STORAGE = 12345
    }
}