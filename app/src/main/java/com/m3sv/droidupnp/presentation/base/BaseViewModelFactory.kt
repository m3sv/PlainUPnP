package com.m3sv.droidupnp.presentation.base

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.m3sv.droidupnp.presentation.main.MainActivityViewModel
import com.m3sv.droidupnp.upnp.UPnPManager
import javax.inject.Inject


class BaseViewModelFactory @Inject constructor(private val manager: UPnPManager) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(manager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}