package com.m3sv.plainupnp.common


interface Toastable {
    fun toast(text: String)

    fun toast(text: Int, arguments: Any? = null)

    fun longToast(text: String)

    fun longToast(text: Int, arguments: Any? = null)
}