package com.m3sv.presentation.base

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar


abstract class BaseActivity : AppCompatActivity() {
    protected fun setToolbarWithBackButton(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }
}