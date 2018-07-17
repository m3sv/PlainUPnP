package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.MutableLiveData
import com.m3sv.droidupnp.presentation.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


class MainFragmentViewModel @Inject constructor(private val repository: GalleryRepository) :
    BaseViewModel() {

    val images = MutableLiveData<HashSet<GalleryRepository.ImageInfo>>()

    fun updateImageList() {
        disposables += repository
            .getAllImages()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = images::postValue, onError = Timber::e)
    }
}