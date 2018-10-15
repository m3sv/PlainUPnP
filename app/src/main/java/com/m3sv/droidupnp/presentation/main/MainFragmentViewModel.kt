package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.LiveData
import com.m3sv.droidupnp.common.Toastable
import com.m3sv.droidupnp.common.Toaster
import com.m3sv.droidupnp.presentation.base.BaseViewModel
import com.m3sv.droidupnp.presentation.main.data.Item
import com.m3sv.droidupnp.upnp.UpnpManager
import javax.inject.Inject


class MainFragmentViewModel @Inject constructor(
    private val upnpManager: UpnpManager,
    private val repository: GalleryRepository,
    toaster: Toaster
) :
    BaseViewModel(), Toastable by toaster, UpnpManager by upnpManager {

    fun getAll(): LiveData<Set<Item>> = repository.getAll()
}