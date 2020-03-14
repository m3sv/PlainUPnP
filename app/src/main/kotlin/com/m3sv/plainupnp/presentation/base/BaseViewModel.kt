package com.m3sv.plainupnp.presentation.base

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable


abstract class BaseViewModel<Intention, State>(initialState: State) : ViewModel() {

    protected val disposables: CompositeDisposable = CompositeDisposable()

    private val _state: MutableLiveData<State> by lazy(LazyThreadSafetyMode.NONE) { MutableLiveData<State>() }

    val state: LiveData<State> = _state

    private var previousState: State = initialState

    init {
        _state.postValue(initialState)
    }

    @MainThread
    protected fun updateState(state: (previousState: State) -> State) {
        val newState = state(previousState)
        previousState = newState
        _state.postValue(newState)
    }

    abstract fun intention(intention: Intention)

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
