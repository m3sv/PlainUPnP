package com.m3sv.plainupnp.upnp.server

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.m3sv.plainupnp.core.persistence.Database
import com.m3sv.plainupnp.upnp.ContentRepository.Companion.AUDIO_PREFIX
import com.m3sv.plainupnp.upnp.ContentRepository.Companion.IMAGE_PREFIX
import com.m3sv.plainupnp.upnp.ContentRepository.Companion.TREE_PREFIX
import com.m3sv.plainupnp.upnp.ContentRepository.Companion.VIDEO_PREFIX
import com.m3sv.plainupnp.upnp.util.PORT
import fi.iki.elonen.NanoHTTPD
import timber.log.Timber
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaServer @Inject constructor(private val context: Context, private val database: Database) :
    SimpleInputStreamServer(
        null,
        PORT, listOf(), true
    ) {

    override fun serve(session: IHTTPSession): Response = try {
        Timber.i("Received request: ${session.uri}")
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
            val id = uri.replace("/", "").split(".").first()
            val mediaId = id.substring(2)
            return when {
                id.startsWith(AUDIO_PREFIX) -> handleGeneric(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaId)
                id.startsWith(VIDEO_PREFIX) -> handleGeneric(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaId)
                id.startsWith(IMAGE_PREFIX) -> handleGeneric(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId)
                id.startsWith(TREE_PREFIX) -> handleTree(Uri.parse(database
                    .fileCacheQueries
                    .selectById(mediaId.toLong())
                    .executeAsOneOrNull()
                    ?.uri
                    ?: error("Not found")
                ), mediaId)
                else -> error("Unknown content type")
            }

        } catch (e: Exception) {
            Timber.e(e, "Error while parsing $uri")
        }

        throw InvalidIdentifierException("$uri was not found in media database")
    }

    private fun handleGeneric(contentUri: Uri, mediaId: String): ServerObject {
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

        error("Object with id $mediaId not found")
    }

    private fun handleTree(contentUri: Uri, mediaId: String): ServerObject {
        val columns =
            arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.MIME_TYPE
            )

        context
            .contentResolver
            .query(
                contentUri,
                columns,
                null,
                null,
                null
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

        error("Object with id $mediaId not found")
    }

    data class ServerObject(val fileUri: Uri, val mime: String, val inputStream: ParcelFileDescriptor)

    companion object {
        private const val WHERE_CLAUSE = MediaStore.MediaColumns._ID + "=?"
    }
}


