package com.m3sv.plainupnp.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.m3sv.plainupnp.di.ViewModelFactory
import dagger.android.support.DaggerFragment
import javax.inject.Inject


abstract class BaseFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    protected inline fun <reified T : ViewModel> getViewModel(): T =
            ViewModelProviders.of(requireActivity(), viewModelFactory).get(T::class.java)

    protected inline fun <T> LiveData<T>.nonNullObserve(crossinline observer: (t: T) -> Unit) {
        this.observe(this@BaseFragment, Observer {
            it?.let(observer)
        })
    }
}