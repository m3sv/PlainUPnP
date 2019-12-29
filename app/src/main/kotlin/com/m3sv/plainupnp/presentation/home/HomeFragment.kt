package com.m3sv.plainupnp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.m3sv.plainupnp.common.utils.disappear
import com.m3sv.plainupnp.common.utils.show
import com.m3sv.plainupnp.databinding.HomeFragmentBinding
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.base.ControlsSheetDelegate
import com.m3sv.plainupnp.presentation.base.ControlsSheetState
import com.m3sv.plainupnp.presentation.main.MainActivity
import com.m3sv.plainupnp.presentation.views.OffsetItemDecoration
import javax.inject.Inject

class HomeFragment : BaseFragment() {

    @Inject
    lateinit var controlsSheetDelegate: ControlsSheetDelegate

    private lateinit var viewModel: HomeViewModel

    private lateinit var contentAdapter: GalleryContentAdapter

    private lateinit var recyclerLayoutManager: LinearLayoutManager

    private lateinit var binding: HomeFragmentBinding

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.execute(HomeIntention.BackPress)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()
        setHasOptionsMenu(true)
    }

    private fun inject() {
        (activity as MainActivity).mainActivitySubComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        addBackPressedDispatcher()
        initContentAdapter()
        initRecyclerView()
        restoreRecyclerState(savedInstanceState)
        observeControlsSheetState()
    }

    private fun observeControlsSheetState() {
        controlsSheetDelegate.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                ControlsSheetState.OPEN -> disableBackPressedCallback()
                ControlsSheetState.CLOSED -> enableBackPressedCallback()
            }
        }
    }

    private fun addBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeState.Loading -> binding.progress.show()
                is HomeState.Success -> {
                    contentAdapter.setWithDiff(state.contentItems)
                    binding.progress.disappear()
                }
            }
        }
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

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.home_toolbar_menu, menu)
//
//        menu.findItem(R.id.home_search).apply {
//            (actionView as SearchView).apply {
//                applySearchViewTransitionAnimation()
//                setSearchQueryListener()
//            }
//
//            setOnMenuItemClickListener { item ->
//                item.expandActionView()
//            }
//        }
//        super.onCreateOptionsMenu(menu, inflater)
//    }

//    private fun SearchView.setSearchQueryListener() {
//        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String): Boolean = false
//
//            override fun onQueryTextChange(newText: String): Boolean {
//                contentAdapter.filter(newText)
//                return true
//            }
//        })
//    }
//
//    private fun SearchView.applySearchViewTransitionAnimation() {
//        findViewById<LinearLayout>(R.id.search_bar).layoutTransition = LayoutTransition()
//    }

    private fun initRecyclerView() {
        recyclerLayoutManager = LinearLayoutManager(requireContext())
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

    private fun restoreRecyclerState(bundle: Bundle?) {
        if (bundle != null)
            recyclerLayoutManager.onRestoreInstanceState(bundle.getParcelable(RECYCLER_STATE))
    }

    private fun enableBackPressedCallback() {
        onBackPressedCallback.isEnabled = true
    }

    private fun disableBackPressedCallback() {
        onBackPressedCallback.isEnabled = false
    }

    companion object {
        private const val RECYCLER_STATE = "recycler_state_key"
    }
}
