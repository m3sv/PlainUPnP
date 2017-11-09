package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.m3sv.droidupnp.upnp.UPnPManager
import org.droidupnp.view.DeviceDisplay

class MainActivityViewModel(val manager: UPnPManager) : ViewModel() {
    val contentDirectories = MutableLiveData<HashSet<DeviceDisplay>>()
}
