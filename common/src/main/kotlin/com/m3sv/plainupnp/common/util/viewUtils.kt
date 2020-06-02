package com.m3sv.plainupnp.common.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


fun View.showSoftInput() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideSoftInput() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(windowToken, 0)
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.disappear() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}
