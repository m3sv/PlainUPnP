package com.m3sv.plainupnp.nanohttpd

import android.net.Uri
import timber.log.Timber
import java.io.*
import java.net.URLEncoder
import java.util.*

open class SimpleWebServer(
    host: String?,
    port: Int,
    private val rootDir: File?,
    private val quiet: Boolean
) : NanoHTTPD(host, port) {

    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20' instead of '+'.
     */
    private fun encodeUri(uri: String): String {
        val newUri = StringBuilder()
        val st = StringTokenizer(uri, "/ ", true)
        while (st.hasMoreTokens()) {
            when (val tok = st.nextToken()) {
                "/" -> newUri.append("/")
                " " -> newUri.append("%20")
                else -> try {
                    newUri.append(URLEncoder.encode(tok, "UTF-8"))
                } catch (ignored: UnsupportedEncodingException) {
                }

            }
        }
        return newUri.toString()
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all headers and HTTP parameters.
     */
    private fun serveFile(
        uri: String,
        header: Map<String, String>,
        homeDir: File?
    ): Response {
        var newUri = uri
        var res: Response? = null

        // Make sure we won't die of an exception later
        if (homeDir?.isDirectory != true) {
            res = Response(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "INTERNAL ERRROR: serveFile(): given homeDir is not a directory."
            )
        }

        if (res == null) {
            // Remove URL arguments
            newUri = newUri.trim { it <= ' ' }.replace(File.separatorChar, '/')
            if (newUri.indexOf('?') >= 0)
                newUri = newUri.substring(0, newUri.indexOf('?'))

            // Prohibit getting out of current directory
            if (newUri.startsWith("src/main") || newUri.endsWith("src/main") || newUri.contains("../"))
                res = Response(
                    Response.Status.FORBIDDEN,
                    MIME_PLAINTEXT,
                    "FORBIDDEN: Won't serve ../ for security reasons."
                )
        }

        var f = File(homeDir, newUri)
        if (res == null && !f.exists()) {
            res = Response(
                Response.Status.NOT_FOUND,
                MIME_PLAINTEXT,
                "Error 404, file not found."
            )
        }

        // List the directory, if necessary
        if (res == null && f.isDirectory) {
            // Browsers get confused without '/' after the
            // directory, send a redirect.
            if (!newUri.endsWith("/")) {
                newUri += "/"
                res = Response(
                    Response.Status.REDIRECT,
                    MIME_HTML,
                    "<html><body>Redirected: <a href=\"" + newUri + "\">" + newUri
                            + "</a></body></html>"
                )
                res.addHeader("Location", newUri)
            }

            if (res == null) {
                // First try index.html and index.htm
                when {
                    File(f, "index.html").exists() -> f = File(homeDir, "$newUri/index.html")
                    File(f, "index.htm").exists() -> f = File(homeDir, "$newUri/index.htm")
                    f.canRead() -> // No index file, list the directory if it is readable
                        res = Response(listDirectory(newUri, f))
                    else -> res = Response(
                        Response.Status.FORBIDDEN,
                        MIME_PLAINTEXT,
                        "FORBIDDEN: No directory listing."
                    )
                }
            }
        }

        try {
            if (res == null) {
                // Get MIME type from file name extension, if possible
                var mime: String? = null
                val dot = f.canonicalPath.lastIndexOf('.')

                if (dot >= 0) {
                    mime = MIME_TYPES[f.canonicalPath.substring(dot + 1)
                        .toLowerCase(Locale.getDefault())]
                }

                if (mime == null) {
                    mime = MIME_DEFAULT_BINARY
                }
//                res = serveFile(f, mime, header)
            }
        } catch (ioe: IOException) {
            res = FORBIDDEN_READING_FAILED
        }

        if (res == null) res = FORBIDDEN_READING_FAILED

        return res
    }

    protected fun serveFile(
        fileUri: Uri,
        mime: String,
        inputStream: FileDescriptor,
        header: Map<String, String>
    ): Response {
        var res: Response = FORBIDDEN_READING_FAILED

        try {
            val eTag = Integer.toHexString((fileUri.toString()).hashCode())

            var startFrom: Long = 0
            var endAt: Long = -1
            var range: String? = header["range"]

            if (range != null && range.startsWith("bytes=")) {
                range = range.substring("bytes=".length)
                val minusIndex = range.indexOf('-')

                if (minusIndex > 0) {
                    startFrom = range.substring(0, minusIndex).toLongOrNull() ?: 0L
                    endAt = range.substring(minusIndex + 1).toLongOrNull() ?: -1L
                }
            }

            val fis = FileInputStream(inputStream)
            val fileLen = fis.available()

            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = Response(
                        Response.Status.RANGE_NOT_SATISFIABLE,
                        MIME_PLAINTEXT,
                        ""
                    ).apply {
                        addHeader("Content-Range", "bytes 0-0/$fileLen")
                        addHeader("ETag", eTag)
                    }
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1L
                    }
                    var newLen = endAt - startFrom
                    if (newLen < 0) {
                        newLen = 0
                    }

                    val dataLen = newLen
                    val bufferedInputStream = object : FileInputStream(inputStream) {
                        override fun available(): Int {
                            return dataLen.toInt()
                        }
                    }
                    bufferedInputStream.skip(startFrom)

                    res = Response(Response.Status.PARTIAL_CONTENT, mime, bufferedInputStream)
                        .apply {
                            addHeader("Content-Length", dataLen.toString())
                            addHeader("Content-Range", "bytes $startFrom-$endAt/$fileLen")
                            addHeader("ETag", eTag)
                        }
                }
            } else {
                res = if (eTag == header["if-none-match"])
                    Response(Response.Status.NOT_MODIFIED, mime, "")
                else {
                    Response(Response.Status.OK, mime, fis)
                        .apply {
                            addHeader("Content-Length", fileLen.toString())
                            addHeader("ETag", eTag)
                        }
                }
            }
        } catch (ioe: IOException) {
            Timber.e(ioe)
        }

        // Announce that the file server accepts partial content requests
        res.addHeader("Accept-Ranges", "bytes")

        return res
    }

    private fun listDirectory(uri: String, f: File): String {
        val heading = "Directory $uri"
        val msg = StringBuilder(
            "<html><head><title>" + heading + "</title><style><!--\n" +
                    "span.dirname { font-weight: bold; }\n" +
                    "span.filesize { font-size: 75%; }\n" +
                    "// -->\n" +
                    "</style>" +
                    "</head><body><h1>" + heading + "</h1>"
        )

        var up: String? = null
        if (uri.length > 1) {
            val u = uri.substring(0, uri.length - 1)
            val slash = u.lastIndexOf('/')
            if (slash >= 0 && slash < u.length) {
                up = uri.substring(0, slash + 1)
            }
        }

        val files = f.list { dir, name -> File(dir, name).isFile } ?: arrayOf()
        files.sort()
        val directories = f.list { dir, name -> File(dir, name).isDirectory } ?: arrayOf()
        directories.sort()
        if (up != null || directories.size + files.size > 0) {
            msg.append("<ul>")
            if (up != null || directories.isNotEmpty()) {
                msg.append("<section class=\"directories\">")
                if (up != null) {
                    msg.append("<li><a rel=\"directory\" href=\"")
                        .append(up)
                        .append("\"><span class=\"dirname\">..</span></a></b></li>")
                }
                for (i in directories.indices) {
                    val dir = directories[i] + "/"
                    msg.append("<li><a rel=\"directory\" href=\"")
                        .append(encodeUri(uri + dir))
                        .append("\"><span class=\"dirname\">")
                        .append(dir)
                        .append("</span></a></b></li>")
                }
                msg.append("</section>")
            }
            if (files.isNotEmpty()) {
                msg.append("<section class=\"files\">")
                for (i in files.indices) {
                    val file = files[i]

                    msg.append("<li><a href=\"")
                        .append(encodeUri(uri + file))
                        .append("\"><span class=\"filename\">")
                        .append(file)
                        .append("</span></a>")

                    val curFile = File(f, file)
                    val len = curFile.length()
                    msg.append("&nbsp;<span class=\"filesize\">(")
                    when {
                        len < 1024 -> msg.append(len).append(" bytes")
                        len < 1024 * 1024 -> msg
                            .append(len / 1024).append(".")
                            .append(len % 1024 / 10 % 100)
                            .append(" KB")
                        else -> msg.append(len / (1024 * 1024))
                            .append(".")
                            .append(len % (1024 * 1024) / 10 % 100)
                            .append(" MB")
                    }
                    msg.append(")</span></li>")
                }
                msg.append("</section>")
            }
            msg.append("</ul>")
        }
        msg.append("</body></html>")
        return msg.toString()
    }

    override fun serve(
        uri: String,
        method: Method,
        headers: Map<String, String>,
        params: Map<String, String>,
        files: Map<String, String>
    ): Response {
        if (!quiet) {
            Timber.d("$method '$uri' ")

            var e = headers.keys.iterator()
            while (e.hasNext()) {
                val value = e.next()
                Timber.d("HDR: '%s'='%s'", value, headers[value])
            }
            e = params.keys.iterator()
            while (e.hasNext()) {
                val value = e.next()
                Timber.d("PRM: '%s'='%s'", value, params[value])
            }
            e = files.keys.iterator()
            while (e.hasNext()) {
                val value = e.next()
                Timber.d("UPLOADED: '%s'='%s'", value, files[value])
            }
        }
        return serveFile(uri, headers, rootDir)
    }

    companion object {
        /**
         * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
         */
        private val MIME_TYPES = mapOf(
            "css" to "text/css",
            "htm" to "text/html",
            "html" to "text/html",
            "xml" to "text/xml",
            "java" to "text/x-java-source, text/java",
            "txt" to "text/plain",
            "asc" to "text/plain",
            "gif" to "image/gif",
            "jpg" to "image/jpeg",
            "jpeg" to "image/jpeg",
            "png" to "image/png",
            "mp3" to "audio/mpeg",
            "m3u" to "audio/mpeg-url",
            "mp4" to "video/mp4",
            "ogv" to "video/ogg",
            "flac" to "audio/flac",
            "flv" to "video/x-flv",
            "mov" to "video/quicktime",
            "swf" to "application/x-shockwave-flash",
            "js" to "application/javascript",
            "pdf" to "application/pdf",
            "doc" to "application/msword",
            "ogg" to "application/x-ogg",
            "zip" to "application/octet-stream",
            "exe" to "application/octet-stream",
            "class" to "application/octet-stream"
        )

        private val FORBIDDEN_READING_FAILED = Response(
            Response.Status.FORBIDDEN,
            MIME_PLAINTEXT,
            "FORBIDDEN: Reading file failed."
        )
    }
}
