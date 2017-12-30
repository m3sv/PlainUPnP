package com.m3sv.presentation.main

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.presentation.base.BaseViewModelFactory
import com.m3sv.droidupnp.presentation.main.MainActivityViewModel
import com.m3sv.presentation.base.BaseActivity
import dagger.android.AndroidInjection
import org.droidupnp.view.SettingsActivity
import javax.inject.Inject


class MainActivity : BaseActivity() {
    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)

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

    private fun refresh() = viewModel.refreshServiceListener()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
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