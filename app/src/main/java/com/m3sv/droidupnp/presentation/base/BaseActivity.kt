package com.m3sv.droidupnp.presentation.base

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.common.NavigationHost
import com.m3sv.droidupnp.di.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


const val THEME_KEY = "is_light_theme"

abstract class BaseActivity : DaggerAppCompatActivity(), NavigationHost {
    protected val disposables = CompositeDisposable()

    var isLightTheme: Boolean = false
        protected set

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        isLightTheme =
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean(THEME_KEY, true)

        if (isLightTheme) {
            setTheme(R.style.AppTheme)
        } else {
            setTheme(R.style.AppTheme_Dark)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    protected fun setAsHomeUp(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
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