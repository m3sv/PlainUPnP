package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.m3sv.droidupnp.upnp.UPnPManager
import org.droidupnp.view.DeviceDisplay

class MainActivityViewModel(private val manager: UPnPManager) : ViewModel() {
    val contentDirectories = MutableLiveData<HashSet<DeviceDisplay>>()

    fun resumeController() {
        manager.controller.resume()
        manager.addObservers()
    }

    fun pauseController() = manager.controller.run {
        pause()
        manager.removeObservers()
        serviceListener?.serviceConnection?.onServiceDisconnected(null)
    }

    fun refreshServiceListener() = manager.controller.serviceListener?.refresh()
}
