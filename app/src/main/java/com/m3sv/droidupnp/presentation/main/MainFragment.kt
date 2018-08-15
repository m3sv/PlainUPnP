package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.m3sv.droidupnp.databinding.MainFragmentBinding
import com.m3sv.droidupnp.presentation.base.BaseFragment
import com.m3sv.droidupnp.presentation.main.data.Item


class MainFragment : BaseFragment() {

    private lateinit var viewModel: MainFragmentViewModel

    private lateinit var binding: MainFragmentBinding

    private lateinit var contentAdapter: GalleryContentAdapter

    private val contentObserver = Observer<Set<Item>> { content ->
        content?.let {
            contentAdapter.setWithDiff(
                GalleryContentAdapter.DiffCallback(
                    contentAdapter.items,
                    it.toList()
                )
            )
        }
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
        binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.vm = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentAdapter = GalleryContentAdapter() {

        }

        binding.content.run {
            layoutManager =
                    GridLayoutManager(requireActivity(), 3, GridLayoutManager.VERTICAL, false)
            adapter = contentAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getAll().observe(contentObserver)
    }

    companion object {
        val TAG = MainFragment::class.java.simpleName

        fun newInstance(): MainFragment = MainFragment().apply {
            arguments = Bundle()
        }
    }
}