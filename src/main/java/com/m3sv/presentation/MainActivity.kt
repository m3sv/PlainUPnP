package com.m3sv.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import org.droidupnp.DrawerFragment
import org.droidupnp.R
import org.droidupnp.controller.cling.Factory
import org.droidupnp.controller.upnp.IUPnPServiceController
import org.droidupnp.model.upnp.IFactory
import org.droidupnp.view.ContentDirectoryFragment
import org.droidupnp.view.SettingsActivity


class MainActivity : AppCompatActivity() {
    private var drawerFragment: DrawerFragment? = null
    private var mainTitle: CharSequence? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (factory == null) factory = Factory()
        if (upnpServiceController == null) upnpServiceController = factory?.createUpnpServiceController(this)
        if (fragmentManager.findFragmentById(R.id.navigation_drawer) is DrawerFragment) {
            drawerFragment = fragmentManager.findFragmentById(R.id.navigation_drawer) as DrawerFragment
            mainTitle = title
            drawerFragment?.setUp(R.id.navigation_drawer, findViewById(R.id.drawer_layout))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXT_STORAGE)
            }
    }

    override fun onResume() {
        super.onResume()
        upnpServiceController?.resume(this)
    }

    override fun onPause() {
        upnpServiceController?.run {
            pause()
            serviceListener?.serviceConnection?.onServiceDisconnected(null)
        }
        super.onPause()
    }

    fun refresh() {
        upnpServiceController?.serviceListener?.refresh()
        val contentDirectoryFragment = contentDirectoryFragment
        contentDirectoryFragment?.refresh()
    }

    fun restoreActionBar() {
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

    override fun onBackPressed() {
        val contentDirectoryFragment = contentDirectoryFragment
        if (contentDirectoryFragment != null && !contentDirectoryFragment.goBack())
            return
        super.onBackPressed()
    }

    companion object {
        val REQUEST_READ_EXT_STORAGE = 12345

        @JvmField
        var upnpServiceController: IUPnPServiceController? = null

        @JvmField
        var factory: IFactory? = null

        var actionBarMenu: Menu? = null

        @JvmStatic
        var contentDirectoryFragment: ContentDirectoryFragment? = null

        @JvmStatic
        fun setSearchVisibility(visible: Boolean) {
            actionBarMenu?.findItem(R.id.action_search)?.isVisible = visible
        }
    }

}