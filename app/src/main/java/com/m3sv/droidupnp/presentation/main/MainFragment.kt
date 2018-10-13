package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.Observer
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.RxTextView
import com.m3sv.droidupnp.databinding.MainFragmentBinding
import com.m3sv.droidupnp.presentation.base.BaseFragment
import com.m3sv.droidupnp.presentation.main.data.Item
import com.m3sv.droidupnp.upnp.DIDLObjectDisplay
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.droidupnp.model.upnp.didl.IDIDLItem
import timber.log.Timber


class MainFragment : BaseFragment() {

    private lateinit var viewModel: MainFragmentViewModel

    private lateinit var binding: MainFragmentBinding

    private lateinit var contentAdapter: GalleryContentAdapter

    private val upnpContentObserver = Observer<List<DIDLObjectDisplay>> { content ->
        content?.let { contentAdapter.setWithDiff(Item.fromDIDLObjectDisplay(content)) }
    }

    private val localContentObserver = Observer<Set<Item>> { content ->
        content?.let { contentAdapter.setWithDiff(it.toList()) }
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

        contentAdapter = GalleryContentAdapter(object : OnItemClickListener {
            override fun onDirectoryClick(itemUri: String, parentId: String?) {
                viewModel.navigateToDirectory(itemUri, parentId)
            }

            override fun onItemClick(item: IDIDLItem, position: Int) {
                viewModel.launchItem(item, position)
            }
        })

        binding.content.run {
            val spanCount =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3
                else 5
            layoutManager =
                    GridLayoutManager(
                        requireActivity(),
                        spanCount,
                        GridLayoutManager.VERTICAL,
                        false
                    )
            adapter = contentAdapter
        }

        disposables += RxTextView.textChanges(binding.filter)
            .subscribeBy(onNext = {
                contentAdapter.filter(it)
            }, onError = Timber::e)

        with(viewModel.contentData) {
            observe(upnpContentObserver)
            value?.let {
                contentAdapter.setWithDiff(Item.fromDIDLObjectDisplay(it))
            }
        }
    }

    companion object {
        val TAG: String = MainFragment::class.java.simpleName

        fun newInstance(): MainFragment = MainFragment().apply {
            arguments = Bundle()
        }
    }
}