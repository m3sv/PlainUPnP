package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.LiveData
import com.m3sv.droidupnp.presentation.base.BaseViewModel
import com.m3sv.droidupnp.presentation.main.data.Item
import timber.log.Timber
import javax.inject.Inject


class MainFragmentViewModel @Inject constructor(private val repository: GalleryRepository) :
    BaseViewModel() {

    fun getAll(): LiveData<Set<Item>> = repository.getAll()
}