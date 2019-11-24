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

        if (savedInstanceState != null)
            restoreRecyclerState(savedInstanceState)

        initContentAdapter()
        initRecyclerView()
        observeState()
    }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_toolbar_menu, menu)

        menu.findItem(R.id.home_search).apply {
            (actionView as SearchView).apply {
                applySearchViewTransitionAnimation()
                setSearchQueryListener()
            }

            setOnMenuItemClickListener { item ->
                item.expandActionView()
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun observeState() {
        viewModel.state.nonNullObserve { state ->
            when (state) {
                is HomeState.Loading -> binding.progress.show()
                is HomeState.Success -> {
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

    private fun initRecyclerView() {
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
    }

    private fun initContentAdapter() {
        contentAdapter = GalleryContentAdapter(Glide.with(this)) {
            viewModel.execute(HomeIntention.ItemClick(it))
        }
    }

    private fun restoreRecyclerState(bundle: Bundle) {
        recyclerLayoutManager.onRestoreInstanceState(bundle.getParcelable(RECYCLER_STATE))
    }

    companion object {
        private const val RECYCLER_STATE = "recycler_state_key"
    }
}
