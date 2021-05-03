package com.m3sv.plainupnp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.m3sv.plainupnp.common.preferences.PreferencesRepository
import com.m3sv.plainupnp.common.util.disappear
import com.m3sv.plainupnp.presentation.home.databinding.HomeFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private lateinit var contentAdapter: GalleryContentAdapter

    private lateinit var recyclerLayoutManager: LinearLayoutManager

    private lateinit var binding: HomeFragmentBinding

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = HomeFragmentBinding.inflate(
        inflater,
        container,
        false
    ).apply {
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        observeState()
        initRecyclerView()
    }

    private fun observeState() {
        viewModel.currentFolderContents.observe(viewLifecycleOwner) { contents ->
            contentAdapter.setWithDiff(contents)
            binding.progress.disappear()
        }

        viewModel.filterText.observe(viewLifecycleOwner) { text ->
            lifecycleScope.launch {
                contentAdapter.filter(text)
            }
        }
    }

    private fun initRecyclerView() {
        contentAdapter = GalleryContentAdapter(
            glide = Glide.with(this),
            preferencesRepository = preferencesRepository,
            onItemClickListener = viewModel::itemClick,
            onLongItemClickListener = viewModel::itemLongClick
        )

        recyclerLayoutManager = LinearLayoutManager(requireContext())

        with(binding.content) {
            setHasFixedSize(true)
            layoutManager = recyclerLayoutManager
            adapter = contentAdapter
            FastScrollerBuilder(this).useMd2Style().build()
        }
    }
}
