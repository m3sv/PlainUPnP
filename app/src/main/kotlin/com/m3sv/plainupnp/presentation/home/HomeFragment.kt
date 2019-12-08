package com.m3sv.plainupnp.presentation.home

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.observe
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
        viewModel = getViewModel()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding
            .inflate(inflater, container, false)
            .apply { setupToolbar() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        addBackPressedDispatcher()
        initContentAdapter()
        initRecyclerView()

        if (savedInstanceState != null)
            restoreRecyclerState(savedInstanceState)
    }

    private fun addBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback {
            backPressDelegate()
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeState.Loading -> binding.progress.show()
                is HomeState.Success -> {
                    contentAdapter.setWithDiff(state.contentItems)
                    binding.run {
                        homeToolbar.title = state.directoryName
                        progress.disappear()
                    }

                    backPressDelegate = if (state.isRoot) {
                        showExitConfirmationDialogDelegate
                    } else {
                        navigateBackDelegate
                    }
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

    private fun restoreRecyclerState(bundle: Bundle) {
        recyclerLayoutManager.onRestoreInstanceState(bundle.getParcelable(RECYCLER_STATE))
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.dialog_exit_title))
            .setMessage(getString(R.string.dialog_exit_body))
            .setPositiveButton(getString(R.string.exit)) { _, _ ->
                // todo clear latest state when finish
                requireActivity().finishAndRemoveTask()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .show()
    }


    private val showExitConfirmationDialogDelegate = {
        showExitConfirmationDialog()
    }

    private val navigateBackDelegate = {
        viewModel.execute(HomeIntention.BackPress)
    }

    private var backPressDelegate = navigateBackDelegate

    private fun HomeFragmentBinding.setupToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(homeToolbar)
    }

    companion object {
        private const val RECYCLER_STATE = "recycler_state_key"
    }
}
