package com.m3sv.plainupnp.common.util

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

val Activity.inputMethodManager: InputMethodManager
    get() = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
