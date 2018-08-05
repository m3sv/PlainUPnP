package com.m3sv.droidupnp.presentation.main

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ArrayAdapter
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.databinding.MainActivityBinding
import com.m3sv.droidupnp.presentation.base.BaseActivity
import com.m3sv.droidupnp.presentation.base.THEME_KEY
import com.m3sv.droidupnp.presentation.settings.SettingsFragment
import org.droidupnp.view.DeviceDisplay
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class MainActivity : BaseActivity() {
    @Inject
    lateinit var viewModel: MainActivityViewModel

    private lateinit var binding: MainActivityBinding

    private lateinit var rendererAdapter: ArrayAdapter<String>

    private lateinit var contentDirectoryAdapter: ArrayAdapter<String>

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
            contentDirectoryAdapter.run {
                clear()
                addAll(it.map { deviceDisplay -> deviceDisplay.device.displayString }.toList())
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)
        binding.vm = viewModel
        binding.setLifecycleOwner(this)

        restoreFragmentState()
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

        binding.controlsSheet.changeTheme.setOnClickListener {
            if (isLightTheme) {
                setTheme(R.style.AppTheme_Dark)
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean(THEME_KEY, false).apply()
                recreate()
            } else {
                setTheme(R.style.AppTheme)
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean(THEME_KEY, true).apply()
                recreate()
            }
        }
    }

    private fun setupBottomNavigation(bottomNavigation: BottomNavigationView) {
        bottomNavigation.setOnNavigationItemSelectedListener {
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
        }
    }

    private fun clearPickers() {
        rendererAdapter.clear()
        contentDirectoryAdapter.clear()
    }

    override fun onStart() {
        super.onStart()
        clearPickers()
        viewModel.resumeController()
    }

    override fun onStop() {
        viewModel.pauseController()
        super.onStop()
    }

//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when (item?.itemId) {
//            R.id.menu_refresh -> viewModel.refreshServiceListener()
//            R.id.menu_settings -> startActivity(
//                Intent(this, SettingsActivity::class.java).addFlags(
//                    Intent.FLAG_ACTIVITY_SINGLE_TOP
//                )
//            )
//            else -> super.onOptionsItemSelected(item)
//        }
//        return super.onOptionsItemSelected(item)
//    }

    private fun restoreFragmentState() {
        when (viewModel.lastFragmentTag) {
            MainFragment.TAG -> navigateToMain()
            SettingsFragment.TAG -> navigateToSettings()
            else -> navigateToMain()
        }
    }

    private fun navigateToMain() {
        val fragment = supportFragmentManager.findFragmentByTag(MainFragment.TAG)
        if (fragment == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, MainFragment.newInstance(), MainFragment.TAG)
                .commit()
        } else {
            supportFragmentManager.popBackStack(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        viewModel.lastFragmentTag = MainFragment.TAG
    }

    private fun navigateToSettings() {
        if (supportFragmentManager.findFragmentByTag(SettingsFragment.TAG) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, SettingsFragment.newInstance(), SettingsFragment.TAG)
                .addToBackStack(null)
                .commit()
        }

        viewModel.lastFragmentTag = SettingsFragment.TAG
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (supportFragmentManager.backStackEntryCount == 0)
            binding.bottomNav.selectedItemId = R.id.nav_home
    }

    companion object {
        private const val REQUEST_READ_EXT_STORAGE = 12345
    }
}