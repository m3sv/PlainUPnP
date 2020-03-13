package com.m3sv.plainupnp.nanohttpd

/**
 * UpnpFactory to create temp file managers.
 */
interface TempFileManagerFactory {
    fun create(): TempFileManager?
}
