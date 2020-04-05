package com.m3sv.plainupnp.presentation.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable


abstract class BaseViewModel<Intention> : ViewModel() {

    protected val disposables: CompositeDisposable = CompositeDisposable()

    abstract fun intention(intention: Intention)

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
