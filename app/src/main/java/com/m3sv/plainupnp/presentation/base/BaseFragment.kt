package com.m3sv.plainupnp.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.m3sv.plainupnp.di.ViewModelFactory
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


abstract class BaseFragment : DaggerFragment() {

    protected val disposables = CompositeDisposable()

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    protected inline fun <reified T : ViewModel> getViewModel(): T =
        ViewModelProviders.of(requireActivity(), viewModelFactory).get(T::class.java)

    protected inline fun <T> LiveData<T>.nonNullObserve(crossinline observer: (t: T) -> Unit) {
        this.observe(this@BaseFragment, Observer {
            it?.let(observer)
        })
    }
}