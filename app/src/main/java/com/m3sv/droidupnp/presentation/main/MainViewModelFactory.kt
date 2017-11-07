package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.m3sv.droidupnp.upnp.UPnPManager
import javax.inject.Inject


class MainViewModelFactory @Inject constructor(val manager: UPnPManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(manager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}