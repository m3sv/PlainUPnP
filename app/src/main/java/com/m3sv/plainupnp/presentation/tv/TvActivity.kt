package com.m3sv.plainupnp.presentation.tv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.jakewharton.rxbinding2.view.RxView
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.Directory
import com.m3sv.plainupnp.data.upnp.RendererState
import com.m3sv.plainupnp.databinding.TvActivityBinding
import com.m3sv.plainupnp.presentation.base.BaseActivity
import com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter
import com.m3sv.plainupnp.presentation.main.MainActivityViewModel
import com.m3sv.plainupnp.presentation.main.MainFragment
import com.m3sv.plainupnp.upnp.LaunchLocally
import com.m3sv.plainupnp.upnp.RenderedItem
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class TvActivity : BaseActivity() {

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var binding: TvActivityBinding

    private lateinit var rendererAdapter: SimpleArrayAdapter<DeviceDisplay>

    private lateinit var contentDirectoryAdapter: SimpleArrayAdapter<DeviceDisplay>

    private fun handleContentDirectories(contentDirectories: Set<DeviceDisplay>) {
        Timber.d("Received new set of content directories: ${contentDirectories.size}")
        contentDirectoryAdapter.setNewItems(contentDirectories.toList())
    }

    private val contentDirectorySpinnerClickListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
        ) {
            Timber.d("Selected item: $position")
            viewModel.selectContentDirectory(contentDirectoryAdapter.getItem(position)?.device)
        }
    }

    private val rendererSpinnerClickListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
        ) {
            Timber.d("Selected renderer: $position")
            viewModel.selectRenderer(rendererAdapter.getItem(position)?.device)
        }
    }

    private fun handleRenderers(renderers: Set<DeviceDisplay>) {
        Timber.d("Received new set of renderers: ${renderers.size}")
        rendererAdapter.setNewItems(renderers.toList())
    }

    private fun handleRendererState(rendererState: RendererState) {
        with(binding) {
            progress.isEnabled = rendererState.state != com.m3sv.plainupnp.data.upnp.UpnpRendererState.State.STOP
            progress.progress = rendererState.progress

            when (rendererState.state) {
                com.m3sv.plainupnp.data.upnp.UpnpRendererState.State.STOP -> {
                    play.setImageResource(com.m3sv.plainupnp.R.drawable.ic_play_arrow)
                    com.jakewharton.rxbinding2.view.RxView.clicks(play)
                            .subscribeBy(onNext = { viewModel.resumePlayback() }, onError = timber.log.Timber::e)
                }

                com.m3sv.plainupnp.data.upnp.UpnpRendererState.State.PLAY -> {
                    play.setImageResource(com.m3sv.plainupnp.R.drawable.ic_pause)
                    com.jakewharton.rxbinding2.view.RxView.clicks(play)
                            .subscribeBy(onNext = { viewModel.pausePlayback() }, onError = timber.log.Timber::e)
                }

                com.m3sv.plainupnp.data.upnp.UpnpRendererState.State.PAUSE -> {
                    play.setImageResource(com.m3sv.plainupnp.R.drawable.ic_play_arrow)
                    com.jakewharton.rxbinding2.view.RxView.clicks(play)
                            .subscribeBy(onNext = { viewModel.resumePlayback() }, onError = timber.log.Timber::e)
                }

                com.m3sv.plainupnp.data.upnp.UpnpRendererState.State.INITIALIZING -> {
                    play.setImageResource(com.m3sv.plainupnp.R.drawable.ic_play_arrow)
                    com.jakewharton.rxbinding2.view.RxView.clicks(play)
                            .subscribeBy(onNext = { viewModel.resumePlayback() }, onError = timber.log.Timber::e)
                }
            }.disposeBy(disposables)
        }
    }

    private fun handleRenderedItem(item: RenderedItem) {
        with(item) {
            com.bumptech.glide.Glide.with(this@TvActivity)
                    .load(first)
                    .apply(third)
                    .into(binding.art)

            binding.title.text = second
        }
    }

    private fun initPickers() {
        with(binding) {
            rendererAdapter = com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter(this@TvActivity)
            kotlin.with(mainRendererDevicePicker) {
                adapter = rendererAdapter
                onItemSelectedListener = rendererSpinnerClickListener
            }

            contentDirectoryAdapter = com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter(this@TvActivity)
            kotlin.with(mainContentDevicePicker) {
                adapter = contentDirectoryAdapter
                onItemSelectedListener = contentDirectorySpinnerClickListener
            }
        }
    }

    private lateinit var mainFragment: MainFragment


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

            viewModel.resumeUpnpController()
        } else {
            mainFragment = supportFragmentManager.findFragmentByTag(MainFragment.TAG) as MainFragment
        }

        with(viewModel) {
            renderers.nonNullObserve(::handleRenderers)
            contentDirectories.nonNullObserve(::handleContentDirectories)
            upnpRendererState.nonNullObserve(::handleRendererState)
            renderedItem.nonNullObserve(::handleRenderedItem)
        }

        initPickers()


        RxView.clicks(binding.next)
                .subscribeBy(onNext = { viewModel.playNext() }, onError = Timber::e)
                .disposeBy(disposables)

        RxView.clicks(binding.previous)
                .subscribeBy(onNext = { viewModel.playPrevious() }, onError = Timber::e)
                .disposeBy(disposables)

        viewModel.launchLocally
                .subscribeBy(onNext = ::launchLocally, onError = Timber::e)
                .disposeBy(disposables)

        requestReadStoragePermission()
    }

    private fun launchLocally(item: LaunchLocally?) {
        item?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.uri)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setDataAndType(Uri.parse(item.uri), item.contentType)
            }

            if (intent.resolveActivity(packageManager) != null)
                startActivity(intent)
            else {
                Toast.makeText(this, R.string.cant_launch_locally, Toast.LENGTH_SHORT).show();
            }
        }
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
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return
        }

        if (supportFragmentManager.backStackEntryCount == 0 && viewModel.currentDirectory is Directory.Home) {
            doubleTapExit()
        } else if (viewModel.currentDirectory != null) {
            viewModel.browsePrevious()
        } else {
            finish()
        }
    }
}