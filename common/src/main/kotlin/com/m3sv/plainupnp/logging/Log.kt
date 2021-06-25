package com.m3sv.plainupnp.logging

interface Log {
    fun e(e: Throwable, remote: Boolean = false)
    fun e(text: String, remote: Boolean = false)
    fun d(text: String)
}
