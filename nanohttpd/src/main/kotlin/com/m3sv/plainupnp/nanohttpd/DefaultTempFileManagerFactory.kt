package com.m3sv.plainupnp.nanohttpd


/**
 * Default strategy for creating and cleaning up temporary files.
 */
private class DefaultTempFileManagerFactory : TempFileManagerFactory {
    override fun create(): TempFileManager = DefaultTempFileManager()
}
