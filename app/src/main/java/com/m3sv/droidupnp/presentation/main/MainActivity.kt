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
import com.m3sv.droidupnp.presentation.main.MainActivityViewModel
import com.m3sv.presentation.base.BaseActivity
import org.droidupnp.DrawerFragment
import org.droidupnp.controller.upnp.IUPnPServiceController
import org.droidupnp.view.SettingsActivity


class MainActivity : BaseActivity() {
    private var drawerFragment: DrawerFragment? = null
    private var mainTitle: CharSequence? = null

    lateinit var controller: IUPnPServiceController

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        
        if (fragmentManager.findFragmentById(R.id.main_navigation_drawer) is DrawerFragment) {
            drawerFragment = fragmentManager.findFragmentById(R.id.main_navigation_drawer) as DrawerFragment
            mainTitle = title
            drawerFragment?.setUp(R.id.main_navigation_drawer, findViewById(R.id.drawer_layout))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXT_STORAGE)
            }
    }

    override fun onResume() {
        super.onResume()
        controller.resume(this)
    }

    override fun onPause() {
        controller.run {
            pause()
            serviceListener?.serviceConnection?.onServiceDisconnected(null)
        }
        super.onPause()
    }

    private fun refresh() {
        controller.serviceListener?.refresh()
    }

    private fun restoreActionBar() {
        val actionBar = supportActionBar
        actionBar?.run {
            setDisplayShowTitleEnabled(true)
            title = mainTitle
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        actionBarMenu = menu
        restoreActionBar()
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
        val REQUEST_READ_EXT_STORAGE = 12345

        @JvmField
        var actionBarMenu: Menu? = null

        @JvmStatic
        fun setSearchVisibility(visible: Boolean) {
            actionBarMenu?.findItem(R.id.action_search)?.isVisible = visible
        }
    }

}