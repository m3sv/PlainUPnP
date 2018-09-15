package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.LiveData
import com.hub.common.Toastable
import com.hub.common.Toaster
import com.m3sv.droidupnp.presentation.base.BaseViewModel
import com.m3sv.droidupnp.presentation.main.data.Item
import com.m3sv.droidupnp.upnp.DIDLObjectDisplay
import com.m3sv.droidupnp.upnp.UpnpManager
import javax.inject.Inject


class MainFragmentViewModel @Inject constructor(
    private val upnpManager: UpnpManager,
    private val repository: GalleryRepository,
    toaster: Toaster
) :
    BaseViewModel(), Toastable by toaster {

    val contentData: LiveData<List<DIDLObjectDisplay>> = upnpManager.contentData

    fun getAll(): LiveData<Set<Item>> = repository.getAll()

    fun navigateToDirectory(directoryId: String, parentId: String?) {
        upnpManager.browseTo(directoryId, parentId)
    }
}