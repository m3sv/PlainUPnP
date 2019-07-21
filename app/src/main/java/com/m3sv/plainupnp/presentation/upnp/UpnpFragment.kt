package com.m3sv.plainupnp.presentation.upnp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.m3sv.plainupnp.common.utils.*
import com.m3sv.plainupnp.databinding.MainFragmentBinding
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.views.OffsetItemDecoration


class UpnpFragment : BaseFragment() {

    private lateinit var viewModel: UpnpViewModel

    private lateinit var binding: MainFragmentBinding

    private lateinit var contentAdapter: GalleryContentAdapter

    private var expanded = false

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

        contentAdapter = GalleryContentAdapter(Glide.with(this)) {
            viewModel.execute(MainFragmentIntention.ItemClick(it))
        }

        with(binding) {
            with(content) {
                setHasFixedSize(true)
                addItemDecoration(OffsetItemDecoration(requireContext(), OffsetItemDecoration.HORIZONTAL))
                layoutManager = LinearLayoutManager(this@UpnpFragment.requireContext())
                adapter = contentAdapter
            }

            with(search) {
                setOnSearchClickListener {
                    if (!expanded)
                        showSearch()
                    else
                        hideSearch()
                }
            }

            filter.addTextChangedListener(onTextChangedListener(contentAdapter::filter))
        }

        viewModel.state.nonNullObserve { state ->
            when (state) {
                is MainFragmentState.Loading -> showProgress()
                is MainFragmentState.Success -> {
                    with(binding) {
                        folderName.text = state.directoryName
                        contentAdapter.setWithDiff(state.items)
                    }
                    hideProgress()
                }
            }
        }
    }

    private fun hideSearch() {
        with(binding) {
            folderName.animate().x(8.dp.toFloat()).setListener(object : AnimatorListenerAdapter() {
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

            folderName.animate().translationX(-folderName.width.toFloat() + 8.dp)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            folderName.disappear()
                        }
                    })

            filter.animate().x(8.dp.toFloat())
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
    }

    private fun hideProgress() {
        binding.progress.disappear()
    }

    companion object {
        private const val IS_EXPANDED = "is_expanded"
    }
}