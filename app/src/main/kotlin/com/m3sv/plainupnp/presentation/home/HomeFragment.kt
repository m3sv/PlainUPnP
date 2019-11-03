package com.m3sv.plainupnp.presentation.home

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.utils.disappear
import com.m3sv.plainupnp.common.utils.show
import com.m3sv.plainupnp.databinding.HomeFragmentBinding
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.views.OffsetItemDecoration

class HomeFragment : BaseFragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var contentAdapter: GalleryContentAdapter
    private lateinit var recyclerLayoutManager: LinearLayoutManager

    private lateinit var binding: HomeFragmentBinding

    /**
     * This can be triggered before [onCreateView] is finished,
     * so we check if binding was initialized before trying to save it's state
     */
    override fun onSaveInstanceState(outState: Bundle) {
        if (this::binding.isInitialized)
            outState.putParcelable(
                RECYCLER_STATE,
                binding.content.layoutManager?.onSaveInstanceState()
            )
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = getViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding.inflate(inflater, container, false).apply {
            (activity as AppCompatActivity).setSupportActionBar(homeToolbar)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerLayoutManager = LinearLayoutManager(requireContext())

        savedInstanceState?.let {
            recyclerLayoutManager.onRestoreInstanceState(it.getParcelable(RECYCLER_STATE))
        }

        contentAdapter = GalleryContentAdapter(Glide.with(this)) {
            viewModel.execute(MainFragmentIntention.ItemClick(it))
        }

        binding.content.run {
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

        observeState()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_toolbar_menu, menu)
        menu.findItem(R.id.home_search).apply {
            (actionView as SearchView).apply {
                applySearchViewTransitionAnimation()
                setSearchQueryListener()
            }

            setOnMenuItemClickListener {
                it.expandActionView()
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun observeState() {
        viewModel.state.nonNullObserve { state ->
            when (state) {
                is MainFragmentState.Loading -> binding.progress.show()
                is MainFragmentState.Success -> {
                    contentAdapter.setWithDiff(state.contentItems)
                    binding.run {
                        homeToolbar.title = state.directoryName
                        progress.disappear()
                    }
                }
            }
        }
    }

    private fun SearchView.setSearchQueryListener() {
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean = false

            override fun onQueryTextChange(newText: String): Boolean {
                contentAdapter.filter(newText)
                return true
            }
        })
    }

    private fun SearchView.applySearchViewTransitionAnimation() {
        findViewById<LinearLayout>(R.id.search_bar).layoutTransition = LayoutTransition()
    }

    companion object {
        private const val RECYCLER_STATE = "recycler_state_key"
    }
}
