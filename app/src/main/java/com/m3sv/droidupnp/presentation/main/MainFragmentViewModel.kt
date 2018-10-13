package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.LiveData
import com.m3sv.droidupnp.common.Toastable
import com.m3sv.droidupnp.common.Toaster
import com.m3sv.droidupnp.presentation.base.BaseViewModel
import com.m3sv.droidupnp.presentation.main.data.Item
import com.m3sv.droidupnp.upnp.DIDLObjectDisplay
import com.m3sv.droidupnp.upnp.DefaultUpnpManager
import org.droidupnp.model.upnp.didl.IDIDLItem
import javax.inject.Inject


class MainFragmentViewModel @Inject constructor(
    private val defaultUpnpManager: DefaultUpnpManager,
    private val repository: GalleryRepository,
    toaster: Toaster
) :
    BaseViewModel(), Toastable by toaster {

    val contentData: LiveData<List<DIDLObjectDisplay>> = defaultUpnpManager.contentData

    fun getAll(): LiveData<Set<Item>> = repository.getAll()

    fun navigateToDirectory(directoryId: String, parentId: String?) {
        defaultUpnpManager.browseTo(directoryId, parentId)
    }

    fun launchItem(item: IDIDLItem, position: Int) {
        defaultUpnpManager.renderItem(item, position)
    }
}