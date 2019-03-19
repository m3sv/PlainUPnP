package com.m3sv.plainupnp.presentation.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding2.widget.RxTextView
import com.m3sv.plainupnp.common.SpaceItemDecoration
import com.m3sv.plainupnp.common.isInstantApp
import com.m3sv.plainupnp.common.utils.*
import com.m3sv.plainupnp.data.upnp.DIDLItem
import com.m3sv.plainupnp.databinding.MainFragmentBinding
import com.m3sv.plainupnp.presentation.base.BaseActivity
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.main.data.toItems
import com.m3sv.plainupnp.presentation.settings.SettingsFragment
import com.m3sv.plainupnp.upnp.BrowseToModel
import com.m3sv.plainupnp.upnp.ContentState
import com.m3sv.plainupnp.upnp.RenderItem
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber


class MainFragment : BaseFragment() {

    private lateinit var viewModel: MainFragmentViewModel

    private lateinit var binding: MainFragmentBinding

    private lateinit var contentAdapter: GalleryContentAdapter

    private var expanded = false

    private fun handleContentState(contentState: ContentState) {
        when (contentState) {
            is ContentState.Success -> {
                with(binding) {
                    folderName.text = contentState.folderName
                    contentAdapter.setWithDiff(contentState.content.toItems())
                    hideProgress()

                    if (isInstantApp(requireContext()) && contentState.content.isEmpty()) {
                        instantAppNotice.show()
                    } else {
                        instantAppNotice.disappear()
                    }
                }
            }

            is ContentState.Loading -> {
                showProgress()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_EXPANDED, expanded)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()

        savedInstanceState?.let {
            expanded = it.getBoolean(IS_EXPANDED, false)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = MainFragmentBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (expanded) {
            with(binding) {
                folderName.hide()

                with(filter) {
                    show()

                    postDelayed(this::showSoftInput, 200)
                }
            }
        }

        contentAdapter = GalleryContentAdapter(Glide.with(this), object : OnItemClickListener {
            override fun onDirectoryClick(
                    directoryName: String,
                    itemUri: String?,
                    parentId: String?
            ) {
                itemUri?.let {
                    viewModel.browseTo(BrowseToModel(itemUri, directoryName, parentId))
                } ?: Timber.e("Item URI is null")
            }

            override fun onItemClick(item: DIDLItem, position: Int) {
                viewModel.renderItem(RenderItem(item, position))
            }
        }, PreferenceManager.getDefaultSharedPreferences(requireContext()))

        binding.content.run {
            setHasFixedSize(true)
            val orientation = resources.configuration.orientation
            val spanCount = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                4
            } else {
                6
            }

            addItemDecoration(SpaceItemDecoration(2.dp))
            layoutManager = GridLayoutManager(
                    requireActivity(),
                    spanCount,
                    RecyclerView.VERTICAL,
                    false
            )

            adapter = contentAdapter
        }

        with(binding.fabMenu) {
            setOnSearchClickListener {
                if (!expanded)
                    showSearch()
                else
                    hideSearch()
            }

            if (requireContext().isRunningOnTv()) {
                // use navigator
                setOnSettingsClickListener { (activity as BaseActivity).navigateTo(SettingsFragment(), SettingsFragment.TAG, true) }
            }
        }

        RxTextView.textChanges(binding.filter)
                .subscribeBy(onNext = contentAdapter::filter, onError = Timber::e)
                .disposeBy(disposables)

        viewModel.serverContent.nonNullObserve(::handleContentState)
    }

    private fun hideSearch() {
        with(binding) {
            folderName.animate().x(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    filter.hideSoftInput()
                    folderName.show()
                }
            })

            filter.animate()
                    .translationX(filter.width.toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            filter.hide()
                            filter.setText("")
                            expanded = false
                        }
                    })
        }
    }

    private fun showSearch() {
        with(binding) {
            filter.translationX = filter.width.toFloat()

            folderName.animate().translationX(-folderName.width.toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            folderName.disappear()
                        }
                    })

            filter.animate().x(0f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            with(filter) {
                                show()
                                requestFocus()
                                showSoftInput()
                                expanded = true
                            }
                        }
                    })
        }
    }

    private fun showProgress() {
        binding.progress.show()
        contentAdapter.setWithDiff(listOf())
    }

    private fun hideProgress() {
        binding.progress.disappear()
    }

    companion object {
        const val TAG = "main_fragment"

        private const val IS_EXPANDED = "is_expanded"

        fun newInstance(): MainFragment = MainFragment().apply {
            arguments = Bundle()
        }
    }
}