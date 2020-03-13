package com.m3sv.plainupnp.nanohttpd

/**
 * Default strategy for creating and cleaning up temporary files.
 *
 * This class stores its files in the standard location (that is,
 * wherever `java.io.tmpdir` points to).  Files are added
 * to an internal list, and deleted when no longer needed (that is,
 * when `clear()` is invoked at the end of processing a
 * request).
 */
class DefaultTempFileManager : TempFileManager {
    private val tmpdir: String = requireNotNull(System.getProperty("java.io.tmpdir"))
    private val tempFiles: MutableList<TempFile> = mutableListOf()

    @Throws(Exception::class)
    override fun createTempFile(): TempFile? {
        val tempFile = DefaultTempFile(tmpdir)
        tempFiles.add(tempFile)
        return tempFile
    }

    override fun clear() {
        for (file in tempFiles) {
            try {
                file.delete()
            } catch (ignored: Exception) {
            }
        }
        tempFiles.clear()
    }
}
