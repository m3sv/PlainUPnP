package com.m3sv.plainupnp.logging

interface Log {
    fun e(e: Throwable, message: String? = null, remote: Boolean = true)
    fun e(text: String, remote: Boolean = true)
    fun d(text: String)
}
