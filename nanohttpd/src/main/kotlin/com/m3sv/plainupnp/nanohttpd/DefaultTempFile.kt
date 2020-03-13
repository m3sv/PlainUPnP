package com.m3sv.plainupnp.nanohttpd

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Default strategy for creating and cleaning up temporary files.
 *
 *
 *
 * [>By default, files are created by `File.createTempFile()` in
 * the directory specified.
 */
class DefaultTempFile(tempDir: String) : TempFile {
    private val file: File = File.createTempFile("NanoHTTPD-", "", File(tempDir))
    private val fileStream: OutputStream = FileOutputStream(file)

    override fun open(): OutputStream = fileStream

    override fun delete() {
        fileStream.safeClose()
        file.delete()
    }

    override fun getName(): String = file.absolutePath
}
