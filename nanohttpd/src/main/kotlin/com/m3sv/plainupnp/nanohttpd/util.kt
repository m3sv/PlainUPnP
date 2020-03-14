@file:JvmName("NanoHttpdUtils")

package com.m3sv.plainupnp.nanohttpd

import timber.log.Timber
import java.io.Closeable
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

/**
 * Common mime type for dynamic content: html
 */
const val MIME_HTML = "text/html"

fun ServerSocket.safeClose() {
    try {
        close()
    } catch (e: IOException) {
        Timber.e(e)
    }
}

fun Socket.safeClose() {
    try {
        close()
    } catch (e: IOException) {
        Timber.e(e)
    }
}


fun Closeable.safeClose() {
    try {
        close()
    } catch (e: IOException) {
        Timber.e(e)
    }
}


