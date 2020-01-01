package com.m3sv.plainupnp.presentation.base

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


abstract class BaseViewModel<Intention, State>(initialState: State) : ViewModel(),
    CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

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
