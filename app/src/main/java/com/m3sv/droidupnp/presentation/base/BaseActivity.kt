package com.m3sv.presentation.base

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.m3sv.droidupnp.R
import com.m3sv.presentation.main.MainActivity


const val THEME_KEY = "is_light_theme"

abstract class BaseActivity : AppCompatActivity() {
    protected var isLightTheme: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        isLightTheme = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(THEME_KEY, true)

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
}