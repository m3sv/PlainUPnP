package com.m3sv.plainupnp.nanohttpd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.URLDecoder

/**
 * Common mime type for dynamic content: plain text
 */
const val MIME_PLAINTEXT = "text/plain"

/**
 * Common mime type for dynamic content: binary
 */
const val MIME_DEFAULT_BINARY = "application/octet-stream"

abstract class NanoHTTPD(private val hostName: String?, private val port: Int) {
    private val tempFileManagerFactory = DefaultTempFileManagerFactory()

    private var serverSocket: ServerSocket? = null

    @Synchronized
    fun start() {
        closeServerSocket()
        startListenerThread()
    }

    fun stop() {
        Timber.d("Stop the server")
        serverSocket?.safeClose()
    }

    /**
     * Override this to customize the server.
     * <p/>
     * <p/>
     * (By default, this delegates to serveFile() and allows directory listing.)
     *
     * @param uri     Percent-decoded URI without parameters, for example "/index.cgi"
     * @param method  "GET", "POST" etc.
     * @param params  Parsed, percent decoded parameters from URI and, in case of POST, data.
     * @param headers Header entries, percent decoded
     * @return HTTP response, see class Response for details
     */
    abstract fun serve(
        uri: String,
        method: Method,
        headers: Map<String, String>,
        params: Map<String, String>,
        files: Map<String, String>
    ): Response

    /**
     * Override this to customize the server.
     *
     * (By default, this delegates to serveFile() and allows directory listing.)
     *
     * @param session The HTTP session
     * @return HTTP response, see class Response for details
     */
    protected open fun serve(session: HTTPSession): Response? {
        val files: MutableMap<String, String> = mutableMapOf()

        try {
            session.parseBody(files)
        } catch (ioe: IOException) {
            return Response(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "SERVER INTERNAL ERROR: IOException: " + ioe.message
            )
        } catch (re: ResponseException) {
            return Response(
                re.status,
                MIME_PLAINTEXT,
                re.message
            )
        }

        return serve(
            uri = session.uri,
            method = session.method,
            headers = session.headers,
            params = session.parms,
            files = files
        )
    }

    /**
     * Decode percent encoded `String` values.
     *
     * @param str the percent encoded `String`
     * @return expanded form of the input, for example "foo%20bar" becomes "foo bar"
     */
    protected open fun decodePercent(str: String?): String? {
        var decoded: String? = null
        try {
            decoded = URLDecoder.decode(str, "UTF8")
        } catch (ignored: UnsupportedEncodingException) {
        }
        return decoded
    }

    private fun closeServerSocket() {
        try {
            serverSocket?.let { if (!it.isClosed) it.close() }
        } catch (e: IOException) {
            Timber.e(e)
        }
    }

    private fun startListenerThread() {
        serverSocket = ServerSocket().apply {
            GlobalScope.launch(Dispatchers.IO) {
                val socketAddress = if (hostName == null)
                    InetSocketAddress(port)
                else
                    InetSocketAddress(hostName, port)

                bind(socketAddress)
                do {
                    try {
                        val finalAccept = accept()
                        val inputStream = finalAccept.getInputStream()
                        if (inputStream == null) {
                            finalAccept.safeClose()
                        } else {
                            var outputStream: OutputStream? = null
                            try {
                                outputStream = finalAccept.getOutputStream()
                                val tempFileManager = tempFileManagerFactory.create()
                                HTTPSession(
                                    this@NanoHTTPD,
                                    tempFileManager,
                                    inputStream,
                                    outputStream
                                ).execute()
                            } catch (e: IOException) {
                                Timber.e(e)
                            } finally {
                                outputStream?.safeClose()
                                inputStream.safeClose()
                                finalAccept.safeClose()
                            }
                        }
                    } catch (e: IOException) {
                        Timber.e(e)
                    }
                } while (!isClosed)
            }
        }
    }
}
