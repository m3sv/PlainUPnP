package com.hub.common

import android.support.annotation.StringRes

interface Toastable {
    fun toast(text: String)

    fun toast(@StringRes text: Int, arguments: Any? = null)

    fun longToast(text: String)

    fun longToast(@StringRes text: Int, arguments: Any? = null)
}