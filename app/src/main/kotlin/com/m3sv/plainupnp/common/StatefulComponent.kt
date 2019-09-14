package com.m3sv.plainupnp.common

import android.os.Bundle

interface StatefulComponent {
    fun onSaveInstanceState(outState: Bundle)
    fun onRestoreInstanceState(savedInstanceState: Bundle)
}