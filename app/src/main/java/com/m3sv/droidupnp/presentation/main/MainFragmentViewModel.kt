package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.LiveData
import com.m3sv.droidupnp.presentation.base.BaseViewModel
import com.m3sv.droidupnp.presentation.main.data.Item
import com.m3sv.droidupnp.upnp.DIDLObjectDisplay
import com.m3sv.droidupnp.upnp.UPnPManager
import javax.inject.Inject


class MainFragmentViewModel @Inject constructor(
    private val uPnPManager: UPnPManager,
    private val repository: GalleryRepository
) :
    BaseViewModel() {

    val contentData: LiveData<List<DIDLObjectDisplay>> = uPnPManager.contentData

    fun getAll(): LiveData<Set<Item>> = repository.getAll()

    fun navigateToDirectory(directoryId: String) {
        uPnPManager.browseTo(directoryId)
    }
}