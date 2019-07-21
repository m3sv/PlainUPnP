package com.m3sv.plainupnp.presentation.base

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable


abstract class BaseViewModel<Intention, State> : ViewModel() {

    protected val disposables: CompositeDisposable = CompositeDisposable()

    @MainThread
    protected fun updateState(state: State) {
        _state.value = state
    }

    private val _state: MutableLiveData<State> = MutableLiveData()

    val state: LiveData<State> = _state

    abstract fun execute(intention: Intention)

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}