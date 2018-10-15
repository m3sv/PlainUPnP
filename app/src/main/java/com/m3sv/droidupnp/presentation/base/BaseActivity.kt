package com.m3sv.droidupnp.presentation.base

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.common.NavigationHost
import com.m3sv.droidupnp.di.ViewModelFactory
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
        super.onDestroy()
        disposables.clear()
    }

    override fun navigateTo(fragment: Fragment, tag: String, addToBackStack: Boolean) {
        val transaction =
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment)

        if (addToBackStack)
            transaction.addToBackStack(null)

        transaction.commit()
    }

    protected inline fun <reified T : ViewModel> BaseActivity.getViewModel(): T {
        return ViewModelProviders.of(this, viewModelFactory).get(T::class.java)
    }

    protected fun <T> LiveData<T>.observe(observer: Observer<T>) {
        observe(this@BaseActivity, observer)
    }
}