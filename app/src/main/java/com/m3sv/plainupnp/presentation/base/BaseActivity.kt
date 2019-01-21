package com.m3sv.plainupnp.presentation.base

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.NavigationHost
import com.m3sv.plainupnp.di.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


abstract class BaseActivity : DaggerAppCompatActivity(), NavigationHost {

    protected val disposables = CompositeDisposable()

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.dark_theme_key), false)) {
            setTheme(R.style.MainActivityThemeDark)
        } else {
            setTheme(R.style.MainActivityThemeLight)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    override fun navigateTo(fragment: Fragment, tag: String, addToBackStack: Boolean) {
        val transaction =
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment)

        if (addToBackStack)
            transaction.addToBackStack(null)

        transaction.commit()
    }

    protected inline fun <reified T : ViewModel> getViewModel(): T =
        ViewModelProviders.of(this, viewModelFactory).get(T::class.java)

    protected inline fun <T> LiveData<T>.nonNullObserve(crossinline observer: (t: T) -> Unit) {
        this.observe(this@BaseActivity, Observer {
            it?.let(observer)
        })
    }
}