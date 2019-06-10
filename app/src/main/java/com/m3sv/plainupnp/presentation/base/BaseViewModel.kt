package com.m3sv.plainupnp.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable


abstract class BaseViewModel<Command, State> : ViewModel() {

    protected val disposables: CompositeDisposable = CompositeDisposable()

    protected fun updateState(state: State) {
        _state.postValue(state)
    }

    private val _state: MutableLiveData<State> = MutableLiveData()

    val state: LiveData<State> = _state

    abstract fun execute(command: Command)

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}