package com.m3sv.plainupnp.upnp.server

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.m3sv.plainupnp.upnp.ContentDirectoryService
import com.m3sv.plainupnp.upnp.util.PORT
import fi.iki.elonen.NanoHTTPD
import timber.log.Timber
import java.nio.charset.StandardCharsets
import javax.inject.Inject


class MediaServer @Inject constructor(private val context: Context) :
    SimpleInputStreamServer(
        null,
        PORT, listOf(), true
    ) {

    override fun serve(session: IHTTPSession): Response = try {
        val obj = getFileServerObject(session.uri)

        Timber.i("Will serve: %s", obj)
        Timber.i("Headers: ${session.headers}")

        serveFile(
            obj.fileUri.toString(),
            session.headers,
            obj.inputStream.fileDescriptor,
            obj.mime
        ).apply {
            addHeader("realTimeInfo.dlna.org", "DLNA.ORG_TLAG=*")
            addHeader("contentFeatures.dlna.org", "")
            addHeader("transferMode.dlna.org", "Streaming")
            addHeader(
                "Server",
                "DLNADOC/1.50 UPnP/1.0 Cling/2.0 PlainUPnP/" + "0.0" + " Android/" + Build.VERSION.RELEASE
            )
        }
    } catch (e: InvalidIdentifierException) {
        val stream = "Error 404, file not found.".byteInputStream(StandardCharsets.UTF_8)
        NanoHTTPD.newFixedLengthResponse(
            Response.Status.NOT_FOUND,
            MIME_PLAINTEXT,
            stream,
            stream.available().toLong()
        )
    }

    inner class InvalidIdentifierException(message: String) : java.lang.Exception(message)

    private fun getFileServerObject(uri: String): ServerObject {
        try {
            // Remove extension
            val dot = uri.lastIndexOf('.')

            val id = if (dot >= 0) uri.substring(0, dot) else uri

            // Try to get media id
            val mediaId = id.substring(3)

            val contentUri = when {
                id.startsWith(AUDIO_PREFIX) -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                id.startsWith(VIDEO_PREFIX) -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                id.startsWith(IMAGE_PREFIX) -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                else -> error("Unknown content type")
            }

            val columns =
                arrayOf(
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.MIME_TYPE
                )

            val whereVal = arrayOf(mediaId)

            context
                .contentResolver
                .query(
                    contentUri, columns,
                    WHERE_CLAUSE, whereVal, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val fileId =
                            cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID))

                        val mime =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))

                        val fileUri = ContentUris.withAppendedId(contentUri, fileId)
                        val inputStream = context.contentResolver.openFileDescriptor(fileUri, "r")

                        return ServerObject(
                            fileUri,
                            mime,
                            inputStream!!
                        )
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "Error while parsing $uri")
        }

        throw InvalidIdentifierException("$uri was not found in media database")
    }

    class ServerObject(val fileUri: Uri, val mime: String, val inputStream: ParcelFileDescriptor)

    companion object {
        private const val AUDIO_PREFIX = "/${ContentDirectoryService.AUDIO_PREFIX}"
        private const val VIDEO_PREFIX = "/${ContentDirectoryService.VIDEO_PREFIX}"
        private const val IMAGE_PREFIX = "/${ContentDirectoryService.IMAGE_PREFIX}"
        private const val WHERE_CLAUSE = MediaStore.MediaColumns._ID + "=?"
    }
}
