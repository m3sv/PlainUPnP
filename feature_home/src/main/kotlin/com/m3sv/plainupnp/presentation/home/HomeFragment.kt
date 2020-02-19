package com.m3sv.plainupnp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.m3sv.plainupnp.App
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.MarginDecoration
import com.m3sv.plainupnp.common.utils.disappear
import com.m3sv.plainupnp.common.utils.show
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.base.ControlsSheetDelegate
import com.m3sv.plainupnp.presentation.base.ControlsSheetState
import com.m3sv.plainupnp.presentation.home.databinding.HomeFragmentBinding
import javax.inject.Inject

class HomeFragment : BaseFragment() {

    @Inject
    lateinit var controlsSheetDelegate: ControlsSheetDelegate

    private lateinit var viewModel: HomeViewModel

    private lateinit var contentAdapter: GalleryContentAdapter

    private lateinit var recyclerLayoutManager: LinearLayoutManager

    private lateinit var binding: HomeFragmentBinding

    private lateinit var glide: RequestManager

    private val handleBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.intention(HomeIntention.BackPress)
        }
    }

    private val showExitDialogCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showExitConfirmationDialog()
        }
    }

    private var onBackPressedCallback: OnBackPressedCallback = showExitDialogCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()
        setHasOptionsMenu(true)
    }

    private fun inject() {
        DaggerHomeComponent
            .factory()
            .create((requireActivity().applicationContext as App).appComponent)
            .inject(this)
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
        glide = Glide.with(this)
        observeState()
        addBackPressedCallback(onBackPressedCallback)
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

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeState.Loading -> binding.progress.show()
                is HomeState.Success -> {
                    when (val directory = state.directory) {
                        is Directory.Root -> {
                            contentAdapter.setWithDiff(directory.content)
                            binding.name.text = directory.name
                            addBackPressedCallback(showExitDialogCallback)
                        }
                        is Directory.SubDirectory -> {
                            contentAdapter.setWithDiff(directory.content)
                            binding.name.text = directory.parentName
                            addBackPressedCallback(handleBackPressedCallback)
                        }
                    }

                    state.filterText.consume()?.let(contentAdapter::filter)
                    binding.progress.disappear()
                    contentAdapter.showThumbnails = state.enableThumbnails
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

    private fun initRecyclerView() {
        contentAdapter = GalleryContentAdapter(glide) { position ->
            viewModel.intention(HomeIntention.ItemClick(position))
        }

        val preloadSizeProvider = contentAdapter.PreloadSizeProvider()
        val modelProvider = contentAdapter.PreloadModelProvider()
        val preloader =
            RecyclerViewPreloader<ContentItem>(glide, modelProvider, preloadSizeProvider, 10)
        binding.content.addOnScrollListener(preloader)

        recyclerLayoutManager = LinearLayoutManager(requireContext())
        binding.content.run {
            setHasFixedSize(true)
            addItemDecoration(MarginDecoration(resources.getDimension(R.dimen.media_item_margin).toInt()))

            layoutManager = recyclerLayoutManager
            adapter = contentAdapter
        }
    }

    private fun restoreRecyclerState(bundle: Bundle?) {
        if (bundle != null)
            recyclerLayoutManager.onRestoreInstanceState(bundle.getParcelable(RECYCLER_STATE))
    }

    private fun enableBackPressedCallback() {
        handleBackPressedCallback.isEnabled = true
        showExitDialogCallback.isEnabled = true
    }

    private fun disableBackPressedCallback() {
        handleBackPressedCallback.isEnabled = false
        showExitDialogCallback.isEnabled = false
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.dialog_exit_title))
            .setMessage(getString(R.string.dialog_exit_body))
            .setPositiveButton(getString(R.string.exit)) { _, _ ->
                requireActivity().finishAndRemoveTask()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .show()
    }

    private fun Fragment.addBackPressedCallback(callback: OnBackPressedCallback) {
        onBackPressedCallback.remove()

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                callback
            )

        onBackPressedCallback = callback
    }

    companion object {
        private const val RECYCLER_STATE = "recycler_state_key"
    }
}
