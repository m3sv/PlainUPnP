package com.m3sv.plainupnp.nanohttpd

/**
 * Temp file manager.
 *
 *
 *
 * Temp file managers are created 1-to-1 with incoming requests, to create and cleanup
 * temporary files created as a result of handling the request.
 */
interface TempFileManager {
    @Throws(Exception::class)
    fun createTempFile(): TempFile?
    fun clear()
}
