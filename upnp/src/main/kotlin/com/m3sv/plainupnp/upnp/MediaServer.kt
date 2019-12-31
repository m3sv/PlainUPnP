package com.m3sv.plainupnp.upnp

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.m3sv.plainupnp.nanohttpd.Method
import com.m3sv.plainupnp.nanohttpd.SimpleWebServer
import timber.log.Timber
import java.io.File
import java.io.InputStream
import javax.inject.Inject


class MediaServer @Inject constructor(private val context: Context) :
    SimpleWebServer(null, PORT, null, true) {

    private val objectMap: MutableMap<String, ServerObject> = mutableMapOf()

    override fun serve(
        uri: String,
        method: Method,
        header: Map<String, String>,
        parms: Map<String, String>,
        files: Map<String, String>
    ): Response? {
        Timber.i("Serve uri: $uri")
        return try {
            val obj = getFileServerObject(uri)

            Timber.i("Will serve: %s", obj.path)

            serveFile(obj.inputStream, File(obj.path), obj.mime, header).apply {
                addHeader("realTimeInfo.dlna.org", "DLNA.ORG_TLAG=*")
                addHeader("contentFeatures.dlna.org", "")
                addHeader("transferMode.dlna.org", "Streaming")
                addHeader(
                    "Server",
                    "DLNADOC/1.50 UPnP/1.0 Cling/2.0 PlainUPnP/" + "0.0" + " Android/" + Build.VERSION.RELEASE
                )
            }
        } catch (e: InvalidIdentifierException) {
            Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Error 404, file not found.")
        }
    }

    inner class InvalidIdentifierException(message: String) : java.lang.Exception(message)

    private fun getFileServerObject(uri: String): ServerObject {
        val serverObject = objectMap[uri]

        if (serverObject != null) {
            return serverObject
        }

        try {
            // Remove extension
            val dot = uri.lastIndexOf('.')

            val id = if (dot >= 0)
                uri.substring(0, dot) else uri

            // Try to get media id
            val mediaId = Integer.parseInt(id.substring(3))

            val contentUri = when {
                id.startsWith("/${ContentDirectoryService.AUDIO_PREFIX}") -> {
                    Timber.v("Ask for audio")
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                id.startsWith("/${ContentDirectoryService.VIDEO_PREFIX}") -> {
                    Timber.v("Ask for video")
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

                id.startsWith("/${ContentDirectoryService.IMAGE_PREFIX}") -> {
                    Timber.v("Ask for image")
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                else -> error("Unknown content type")
            }

            val columns =
                arrayOf(
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.DATA,
                    MediaStore.MediaColumns.MIME_TYPE
                )

            val where = MediaStore.MediaColumns._ID + "=?"
            val whereVal = arrayOf(mediaId.toString())

            context.contentResolver.query(contentUri, columns, where, whereVal, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val fileId =
                            cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID))

                        val mime =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))

                        val fileUri = ContentUris.withAppendedId(contentUri, fileId)
                        val inputStream = context.contentResolver.openInputStream(fileUri)

                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                            ?.let { path ->
                                val result = ServerObject(path, mime, inputStream!!)
                                objectMap[id] = result
                                return result
                            }
                    }
                }

        } catch (e: Exception) {
            Timber.e(e, "Error while parsing $uri")
        }

        throw InvalidIdentifierException("$uri was not found in media database")
    }

    class ServerObject(val path: String, val mime: String, val inputStream: InputStream)
}
