package com.m3sv.presentation.main

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.databinding.MainActivityBinding
import com.m3sv.droidupnp.presentation.base.BaseViewModelFactory
import com.m3sv.droidupnp.presentation.main.MainActivityViewModel
import com.m3sv.presentation.base.BaseActivity
import dagger.android.AndroidInjection
import org.droidupnp.view.DeviceDisplay
import org.droidupnp.view.SettingsActivity
import timber.log.Timber
import javax.inject.Inject


class MainActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: MainActivityViewModel
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

    private val contentDiscoveryObserver = Observer<Set<DeviceDisplay>> {
        it?.run {
            Timber.d("Received new set of content directories: ${it.size}")
            contentDirectoryAdapter.run {
                clear()
                addAll(it.map { deviceDisplay -> deviceDisplay.device.displayString }.toList())
                notifyDataSetChanged()
            }
        }
    }

    private val drawerToggle by lazy {
        object : ActionBarDrawerToggle(this, binding.drawerContainer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        binding.drawerContainer.addDrawerListener(drawerToggle)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)
        viewModel.let {
            it.renderersObservable.observe(this, renderersObserver)
            it.contentDirectoriesObservable.observe(this, contentDiscoveryObserver)
        }

        rendererAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.mainRendererDevicePicker.adapter = rendererAdapter

        contentDirectoryAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.mainContentDevicePicker.adapter = contentDirectoryAdapter

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXT_STORAGE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeController()
    }

    override fun onPause() {
        viewModel.pauseController()
        super.onPause()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    private fun refresh() = viewModel.refreshServiceListener()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (drawerToggle.onOptionsItemSelected(item))
            return true

        when (item?.itemId) {
            R.id.menu_refresh -> refresh()
            R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            R.id.menu_quit -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private val REQUEST_READ_EXT_STORAGE = 12345
    }
}