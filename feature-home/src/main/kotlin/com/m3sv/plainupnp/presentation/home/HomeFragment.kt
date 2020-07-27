package com.m3sv.plainupnp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.m3sv.plainupnp.common.util.disappear
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.home.databinding.HomeFragmentBinding
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import javax.inject.Inject

class HomeFragment : BaseFragment() {

    @Inject
    lateinit var showThumbnailsUseCase: ShowThumbnailsUseCase

    private lateinit var viewModel: HomeViewModel

    private lateinit var contentAdapter: GalleryContentAdapter

    private lateinit var recyclerLayoutManager: LinearLayoutManager

    private lateinit var binding: HomeFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        viewModel = getViewModel()
        if (savedInstanceState == null)
            viewModel.requestCurrentFolderContents()
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
        observeState()
        initRecyclerView()

        if (savedInstanceState != null) restoreRecyclerState(savedInstanceState)
    }

    private fun observeState() {
        viewModel.currentFolderContents.observe(viewLifecycleOwner) { folder ->
            contentAdapter.setWithDiff(folder.contents)
            binding.progress.disappear()
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
            GalleryContentAdapter(
                glide = Glide.with(this),
                showThumbnails = showThumbnailsUseCase,
                onItemClickListener = viewModel::itemClick
            )

        recyclerLayoutManager = LinearLayoutManager(requireContext())
        with(binding.content) {
            setHasFixedSize(true)
            layoutManager = recyclerLayoutManager
            adapter = contentAdapter
            FastScrollerBuilder(this).useMd2Style().build()
        }
    }

    private fun restoreRecyclerState(bundle: Bundle) {
        recyclerLayoutManager.onRestoreInstanceState(bundle.getParcelable(RECYCLER_STATE))
    }

    companion object {
        private const val RECYCLER_STATE = "recycler_state_key"
    }
}
