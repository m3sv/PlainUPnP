package com.m3sv.droidupnp.presentation.main

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.databinding.MainActivityBinding
import com.m3sv.droidupnp.presentation.base.BaseActivity
import com.m3sv.droidupnp.presentation.base.THEME_KEY
import com.m3sv.droidupnp.presentation.settings.SettingsActivity
import org.droidupnp.view.DeviceDisplay
import timber.log.Timber
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initMainFragment()
        initObservers()
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

        binding.changeTheme.setOnClickListener {
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

    private fun initObservers() {
        with(viewModel) {
            renderersObservable.observe(this@MainActivity, renderersObserver)
            contentDirectoriesObservable.observe(this@MainActivity, contentDirectoriesObserver)
        }
    }

    private fun setupPickers() {
        binding.run {
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

    override fun onResume() {
        super.onResume()
        clearPickers()
        viewModel.resumeController()
    }

    override fun onPause() {
        viewModel.pauseController()
        super.onPause()
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

    private fun initMainFragment() {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.container, MainFragment.newInstance())
            commit()
        }
    }

    companion object {
        private const val REQUEST_READ_EXT_STORAGE = 12345
    }
}