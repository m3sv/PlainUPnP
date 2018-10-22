package com.m3sv.plainupnp.common

import android.support.v4.app.Fragment


interface NavigationHost {
    fun navigateTo(fragment: Fragment, tag: String, addToBackStack: Boolean)
}