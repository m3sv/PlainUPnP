package com.m3sv.plainupnp.upnp

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.utils.getSettingContentDirectoryName
import fi.iki.elonen.SimpleWebServer
import fi.iki.elonen.nanohttpd.Method
import fi.iki.elonen.nanohttpd.NanoHTTPD
import org.droidupnp.legacy.mediaserver.ContentDirectoryService
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import timber.log.Timber
import java.io.File
import java.net.InetAddress
import java.util.*


class MediaServer(private val context: Context, private val localAddress: InetAddress) :
    SimpleWebServer(null, PORT, null, true) {

    val localDevice: LocalDevice by lazy(mode = LazyThreadSafetyMode.NONE) {
        val details = DeviceDetails(
            getSettingContentDirectoryName(context),
            ManufacturerDetails(
                context.getString(R.string.app_name),
                context.getString(R.string.app_url)
            ),
            ModelDetails(context.getString(R.string.app_name), context.getString(R.string.app_url)),
            context.getString(R.string.app_name), version
        )

        val validationErrors = details.validate()

        for (error in validationErrors) {
            Timber.e("Validation pb for property " + error.propertyName)
            Timber.e("Error is " + error.message)
        }

        val type = UDADeviceType("MediaServer", 1)

        LocalDevice(DeviceIdentity(udn), type, details, localService)
    }

    private val udn: UDN by lazy(mode = LazyThreadSafetyMode.NONE) {
        UDN.valueOf(UUID(0, 10).toString())
    }

    private val address by lazy(mode = LazyThreadSafetyMode.NONE) { "${localAddress.getHostAddress()}:$PORT" }

    private val localService: LocalService<*>

    private val objectMap: MutableMap<String, ServerObject> = mutableMapOf()

    private val version: String by lazy(mode = LazyThreadSafetyMode.NONE) {
        var result = "1.0"
        try {
            result = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "Application version name not found")
        }
        result
    }

    init {
        Timber.i("Creating media server!")

        localService = (AnnotationLocalServiceBinder()
            .read(ContentDirectoryService::class.java) as LocalService<ContentDirectoryService>).apply {
            manager = DefaultServiceManager<ContentDirectoryService>(
                this,
                ContentDirectoryService::class.java
            )
        }

        with(localService.manager.implementation as ContentDirectoryService) {
            context = this@MediaServer.context
            baseURL = address
        }
    }

    override fun serve(
        uri: String,
        method: Method,
        header: Map<String, String>,
        parms: Map<String, String>,
        files: Map<String, String>
    ): NanoHTTPD.Response? {
        Timber.i("Serve uri: $uri")

        try {
            val obj = getFileServerObject(uri)

            Timber.i("Will serve " + obj.path)

            return serveFile(File(obj.path), obj.mime, header).apply {
                addHeader("realTimeInfo.dlna.org", "DLNA.ORG_TLAG=*")
                addHeader("contentFeatures.dlna.org", "")
                addHeader("transferMode.dlna.org", "Streaming")
                addHeader(
                    "Server",
                    "DLNADOC/1.50 UPnP/1.0 Cling/2.0 PlainUPnP/" + version + " Android/" + Build.VERSION.RELEASE
                )
            }
        } catch (e: InvalidIdentifierException) {
            return NanoHTTPD.Response(
                NanoHTTPD.Response.Status.NOT_FOUND,
                NanoHTTPD.MIME_PLAINTEXT,
                "Error 404, file not found."
            )
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

            when {
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

                else -> null
            }?.let { contentUri ->
                val columns =
                    arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE)
                val where = MediaStore.MediaColumns._ID + "=?"
                val whereVal = arrayOf("" + mediaId)

                val cursor =
                    context.contentResolver.query(contentUri, columns, where, whereVal, null)

                cursor?.takeIf { it.moveToFirst() }?.run {
                    val path = getString(getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                    val mime = getString(getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))

                    path?.let {
                        val result = ServerObject(it, mime)
                        objectMap[id] = result
                        return result
                    }

                    close()
                }
            }

        } catch (e: Exception) {
            Timber.e(e, "Error while parsing $uri")
        }

        throw InvalidIdentifierException("$uri was not found in media database")
    }

    class ServerObject(val path: String, val mime: String)

    companion object {
        private const val PORT = 8192
    }
}