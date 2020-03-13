package com.m3sv.plainupnp.nanohttpd

import java.io.OutputStream


/**
 * A temp file.
 * <p>
 * <p>Temp files are responsible for managing the actual temporary storage and cleaning
 * themselves up when no longer needed.</p>
 */
interface TempFile {

    @Throws(Exception::class)
    fun open(): OutputStream?

    @Throws(Exception::class)
    fun delete()

    fun getName(): String?
}
