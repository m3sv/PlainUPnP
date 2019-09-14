package com.m3sv.plainupnp.presentation.home

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.m3sv.plainupnp.common.utils.*
import com.m3sv.plainupnp.databinding.ContentFragmentBinding
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.views.OffsetItemDecoration


class HomeFragment : BaseFragment() {

    private lateinit var viewModel: HomeViewModel

    private lateinit var binding: ContentFragmentBinding

    private lateinit var contentAdapter: GalleryContentAdapter

    private lateinit var recyclerLayoutManager: LinearLayoutManager

    private var expanded = false

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(IS_EXPANDED, expanded)
        outState.putParcelable(RECYCLER_STATE, binding.content.layoutManager?.onSaveInstanceState())
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ContentFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerLayoutManager = LinearLayoutManager(requireContext())

        savedInstanceState?.let {
            expanded = it.getBoolean(IS_EXPANDED, false)
            recyclerLayoutManager.onRestoreInstanceState(it.getParcelable(RECYCLER_STATE))
        }

        updateFilter()

        contentAdapter = GalleryContentAdapter(Glide.with(this)) {
            viewModel.execute(MainFragmentIntention.ItemClick(it))
        }

        with(binding.content) {
            setHasFixedSize(true)
            addItemDecoration(
                OffsetItemDecoration(
                    requireContext(),
                    OffsetItemDecoration.HORIZONTAL
                )
            )
            layoutManager = recyclerLayoutManager
            adapter = contentAdapter
            (itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        }

        with(binding.search) {
            setOnSearchClickListener {
                if (!expanded)
                    showSearch()
                else
                    hideSearch()
            }
        }

        binding.filter.addTextChangedListener(onTextChangedListener(contentAdapter::filter))

        observeState()
    }

    private fun observeState() {
        viewModel.state.nonNullObserve { state ->
            when (state) {
                is MainFragmentState.Loading -> binding.progress.show()
                is MainFragmentState.Success -> {
                    contentAdapter.setWithDiff(state.contentItems)
                    with(binding) {
                        folderName.text = state.directoryName
                        progress.disappear()
                    }
                }
            }
        }
    }

    private fun updateFilter() {
        if (expanded) {
            binding.folderName.hide()

            with(binding.filter) {
                show()
                postDelayed(this::showSoftInput, 200)
            }
        }
    }

    private fun hideSearch() {
        ObjectAnimator.ofFloat(binding.folderName, View.TRANSLATION_X, 0f).start()

        with(binding.filter) {
            hideSoftInput()
            hide()
            setText("")
        }

        expanded = false
    }

    private fun showSearch() {
        val filter = binding.filter

        ObjectAnimator.ofFloat(binding.folderName, View.TRANSLATION_X, filter.width.toFloat())
            .start()

        with(filter) {
            show()
            requestFocus()
            showSoftInput()
            expanded = true
        }
    }

    companion object {
        private const val IS_EXPANDED = "is_expanded"
        private const val RECYCLER_STATE = "recycler_state_key"
    }
}