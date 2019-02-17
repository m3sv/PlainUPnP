package com.m3sv.plainupnp.presentation.tv

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.Directory
import com.m3sv.plainupnp.databinding.TvActivityBinding
import com.m3sv.plainupnp.presentation.base.BaseActivity
import com.m3sv.plainupnp.presentation.main.MainActivityViewModel
import com.m3sv.plainupnp.presentation.main.MainFragment
import com.m3sv.plainupnp.presentation.main.SearchClickListener
import timber.log.Timber

class TvActivity : BaseActivity() {

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var binding: TvActivityBinding

    private fun handleContentDirectories(contentDirectories: Set<DeviceDisplay>) {
        Timber.d("Received new set of content directories: ${contentDirectories.size}")
        viewModel.selectContentDirectory(contentDirectories.toList()[0].device)

//        contentDirectoryAdapter.setNewItems(contentDirectories.toList())
    }

    private lateinit var mainFragment: MainFragment

    var searchListener: SearchClickListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.tv_activity)

        viewModel = getViewModel()

        if (savedInstanceState == null) {
            mainFragment = MainFragment.newInstance()

            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.container, mainFragment, MainFragment.TAG)
                    .commit()

            searchListener = mainFragment

            viewModel.resumeUpnpController()
        } else {
            mainFragment = supportFragmentManager.findFragmentByTag(MainFragment.TAG) as MainFragment
            searchListener = mainFragment
        }

        with(viewModel) {
            //            renderers.nonNullObserve(::handleRenderers)
            contentDirectories.nonNullObserve(::handleContentDirectories)
//            rendererState.nonNullObserve(::handleRendererState)
//            renderedItem.nonNullObserve(::handleRenderedItem)
        }

        binding.search.setOnClickListener { searchListener?.onSearchClicked() }

        requestReadStoragePermission()
    }

    override fun onStart() {
        super.onStart()
        with(viewModel) {
            resumeUpnp()
            resumeRendererUpdate()
        }
    }

    override fun onStop() {
        with(viewModel) {
            pauseRendererUpdate()
            pauseUpnp()
        }
        super.onStop()
    }

    override fun onBackPressed() {
//        if (supportFragmentManager.backStackEntryCount == 1) {
//            binding.bottomNav.selectedItemId = R.id.nav_home
//            return
//        }

        if (supportFragmentManager.backStackEntryCount == 0 && viewModel.currentDirectory is Directory.Home) {
            doubleTapExit()
        } else if (viewModel.currentDirectory != null) {
            viewModel.browsePrevious()
        } else {
            finish()
        }
    }
}