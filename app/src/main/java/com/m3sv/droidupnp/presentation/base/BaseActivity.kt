package com.m3sv.presentation.base

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.di.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


const val THEME_KEY = "is_light_theme"

abstract class BaseActivity : DaggerAppCompatActivity() {
    protected var isLightTheme: Boolean = false

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

    protected fun setToolbarWithBackButton(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    protected inline fun <reified T : ViewModel> BaseActivity.getViewModel(): T {
        return ViewModelProviders.of(this, viewModelFactory).get(T::class.java)
    }
}