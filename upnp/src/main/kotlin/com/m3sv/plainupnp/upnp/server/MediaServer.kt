package com.m3sv.plainupnp.upnp.server

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.m3sv.plainupnp.core.persistence.Database
import com.m3sv.plainupnp.logging.Log
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl.Companion.AUDIO_PREFIX
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl.Companion.IMAGE_PREFIX
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl.Companion.TREE_PREFIX
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl.Companion.VIDEO_PREFIX
import com.m3sv.plainupnp.upnp.util.PORT
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaServer @Inject constructor(
    private val application: Application,
    private val database: Database,
    private val log: Log
) : SimpleInputStreamServer(null, PORT, listOf(), true) {

    private val serverScope = CoroutineScope(Executors.newFixedThreadPool(8).asCoroutineDispatcher())

    init {
        setAsyncRunner(object : AsyncRunner {
            private val running = mutableListOf<ClientHandler>()
            private val mutex = Mutex()

            override fun closeAll() {
                serverScope.launch { mutex.withLock { running.forEach { it.close() } } }
            }

            override fun closed(clientHandler: ClientHandler) {
                serverScope.launch {
                    mutex.withLock { running.remove(clientHandler) }
                }
            }

            override fun exec(code: ClientHandler) {
                serverScope.launch {
                    mutex.withLock { running.add(code) }
                    code.run()
                }
            }
        })

    }

    override fun serve(session: IHTTPSession): Response = try {
        Timber.i("Received request: ${session.uri}")
        val obj = getFileServerObject(session.uri)

        Timber.i("Will serve: %s", obj)
        Timber.i("Headers: ${session.headers}")

        serveFile(
            obj.fileUri.toString(),
            session.headers,
            obj.inputStream,
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
                id.startsWith(TREE_PREFIX) -> handleTree(
                    Uri.parse(
                        database
                            .fileCacheQueries
                            .selectById(mediaId.toLong())
                            .executeAsOneOrNull()
                            ?.uri
                            ?: error("Not found")
                    ), mediaId
                )
                else -> error("Unknown content type")
            }

        } catch (e: Exception) {
            log.e(e, "Error while parsing $uri")
        }

        throw InvalidIdentifierException("$uri was not found in media database").apply(log::e)
    }

    private fun handleGeneric(contentUri: Uri, mediaId: String): ServerObject {
        val columns =
            arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.MIME_TYPE
            )

        val whereVal = arrayOf(mediaId)

        application
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
                    val inputStream = application.contentResolver.openInputStream(fileUri)

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

        application
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
                    val inputStream = application.contentResolver.openInputStream(fileUri)

                    return ServerObject(
                        fileUri,
                        mime,
                        inputStream!!
                    )
                }
            }

        error("Object with id $mediaId not found")
    }

    data class ServerObject(val fileUri: Uri, val mime: String, val inputStream: InputStream)

    companion object {
        private const val WHERE_CLAUSE = MediaStore.MediaColumns._ID + "=?"
    }
}


