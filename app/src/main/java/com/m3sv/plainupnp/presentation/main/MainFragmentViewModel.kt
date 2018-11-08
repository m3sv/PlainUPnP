package com.m3sv.plainupnp.presentation.main

import android.arch.lifecycle.LiveData
import com.m3sv.plainupnp.common.Toastable
import com.m3sv.plainupnp.common.Toaster
import com.m3sv.plainupnp.data.upnp.DIDLItem
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.presentation.main.data.Item
import com.m3sv.plainupnp.upnp.UpnpManager
import java.text.FieldPosition
import javax.inject.Inject


class MainFragmentViewModel @Inject constructor(
    private val upnpManager: UpnpManager,
    private val repository: GalleryRepository,
    toaster: Toaster
) :
    BaseViewModel(), Toastable by toaster, UpnpManager by upnpManager {

    fun getAll(): LiveData<Set<Item>> = repository.getAll()
}