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


abstract class BaseViewModel<Intention, State> : ViewModel(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    protected val disposables: CompositeDisposable = CompositeDisposable()

    @MainThread
    protected fun updateState(state: State) {
        _state.postValue(state)
    }

    private val _state: MutableLiveData<State> by lazy(LazyThreadSafetyMode.NONE) { MutableLiveData<State>() }

    val state: LiveData<State> = _state

    abstract fun execute(intention: Intention)

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
