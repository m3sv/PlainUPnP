package com.m3sv.plainupnp.nanohttpd


/**
 * Default strategy for creating and cleaning up temporary files.
 */
class DefaultTempFileManagerFactory : TempFileManagerFactory {
    override fun create(): TempFileManager = DefaultTempFileManager()
}
