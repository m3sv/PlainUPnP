package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.m3sv.droidupnp.presentation.base.BaseViewModel
import com.m3sv.droidupnp.presentation.main.data.ImageInfo
import timber.log.Timber
import javax.inject.Inject


class MainFragmentViewModel @Inject constructor(private val repository: GalleryRepository) :
    BaseViewModel() {

    fun getAllImages(): LiveData<HashSet<ImageInfo>> {
        return repository.getImages()
    }

    override fun onCleared() {
        Timber.d("onCleared called")
    }
}