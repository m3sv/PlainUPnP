package com.m3sv.plainupnp.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


abstract class BaseViewModel<Intention, State> : ViewModel(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    protected val disposables: CompositeDisposable = CompositeDisposable()

    protected suspend fun updateState(state: State) {
        withContext(Dispatchers.Main) {
            _state.value = state
        }
    }

    private val _state: MutableLiveData<State> = MutableLiveData()

    val state: LiveData<State> = _state

    abstract fun execute(intention: Intention)

    override fun onCleared() {
        job.cancel()
        disposables.clear()
        super.onCleared()
    }
}
