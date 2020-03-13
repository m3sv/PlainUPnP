@file:JvmName("NanoHttpdUtils")

package com.m3sv.plainupnp.nanohttpd

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
    }
}

fun Socket.safeClose() {
    try {
        close()
    } catch (e: IOException) {
    }
}


fun Closeable.safeClose() {
    try {
        close()
    } catch (e: IOException) {
    }
}


