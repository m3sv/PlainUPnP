package com.m3sv.droidupnp.common

import android.support.v4.app.Fragment


interface NavigationHost {
    fun navigateTo(fragment: Fragment, tag: String, addToBackStack: Boolean)
}