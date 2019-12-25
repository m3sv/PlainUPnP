package com.m3sv.plainupnp.presentation.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.m3sv.plainupnp.di.ViewModelFactory
import javax.inject.Inject


abstract class BaseFragment : Fragment() {

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
