package com.m3sv.plainupnp.common

import androidx.fragment.app.Fragment


interface NavigationHost {
    fun navigateTo(fragment: Fragment, tag: String, addToBackStack: Boolean)
}