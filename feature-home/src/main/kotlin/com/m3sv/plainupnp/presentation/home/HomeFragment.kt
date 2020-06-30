package com.m3sv.plainupnp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.m3sv.plainupnp.common.util.disappear
import com.m3sv.plainupnp.common.util.show
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.base.ControlsSheetDelegate
import com.m3sv.plainupnp.presentation.base.ControlsSheetState
import com.m3sv.plainupnp.presentation.home.databinding.HomeFragmentBinding
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import javax.inject.Inject

class HomeFragment : BaseFragment() {

    @Inject
    lateinit var controlsSheetDelegate: ControlsSheetDelegate

    @Inject
    lateinit var showThumbnailsUseCase: ShowThumbnailsUseCase

    private lateinit var viewModel: HomeViewModel

    private lateinit var contentAdapter: GalleryContentAdapter

    private lateinit var recyclerLayoutManager: LinearLayoutManager

    private lateinit var binding: HomeFragmentBinding

    private val handleBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel.backPress()
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
        (requireContext().applicationContext as HomeComponentProvider).homeComponent.inject(this)
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
        setBackPressedCallback(onBackPressedCallback)
        observeState()
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
                            setBackPressedCallback(showExitDialogCallback)
                            binding.emptyHomeView.root.isVisible = false
                        }
                        is Directory.SubDirectory -> {
                            contentAdapter.setWithDiff(directory.content)
                            binding.name.text = directory.parentName
                            setBackPressedCallback(handleBackPressedCallback)
                            binding.emptyHomeView.root.isVisible = false
                        }
                        is Directory.None -> {
                            contentAdapter.setWithDiff(listOf())
                            binding.name.text = ""
                            setBackPressedCallback(showExitDialogCallback)
                            binding.emptyHomeView.root.isVisible = true
                        }
                    }

                    binding.progress.disappear()
                }
            }
        }

        viewModel.filterText.observe(viewLifecycleOwner) { text ->
            lifecycleScope.launch {
                contentAdapter.filter(text)
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
        contentAdapter =
            GalleryContentAdapter(Glide.with(this), showThumbnailsUseCase, viewModel::itemClick)

        recyclerLayoutManager = LinearLayoutManager(requireContext())
        binding.content.run {
            setHasFixedSize(true)
            layoutManager = recyclerLayoutManager
            adapter = contentAdapter
            FastScrollerBuilder(this).useMd2Style().build()
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

    private fun Fragment.setBackPressedCallback(callback: OnBackPressedCallback) {
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
