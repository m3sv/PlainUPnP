package com.m3sv.plainupnp.upnp.cleanslate

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import com.m3sv.plainupnp.ContentCache
import com.m3sv.plainupnp.upnp.*
import com.m3sv.plainupnp.upnp.mediacontainers.*
import com.m3sv.plainupnp.upnp.resourceproviders.LocalServiceResourceProvider
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException
import org.fourthline.cling.support.contentdirectory.DIDLParser
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.BrowseResult
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.SortCriterion
import org.fourthline.cling.support.model.container.Container
import timber.log.Timber
import java.util.*
import kotlin.LazyThreadSafetyMode.NONE

class LocalUpnpDevice : AbstractContentDirectoryService() {

    lateinit var context: Context

    lateinit var baseURL: String

    lateinit var sharedPref: SharedPreferences

    lateinit var cache: ContentCache

    private val appName by lazy(mode = NONE) { context.getString(R.string.app_name) }

    private val stringSplitter = TextUtils.SimpleStringSplitter(SEPARATOR)

    @Throws(ContentDirectoryException::class)
    override fun browse(
        objectID: String,
        browseFlag: BrowseFlag,
        filter: String,
        firstResult: Long,
        maxResults: Long,
        orderby: Array<SortCriterion>
    ): BrowseResult {
        Timber.d("Will browse $objectID")

        try {
            stringSplitter.setString(objectID)

            var type = -1
            val subtype = mutableListOf<Int>()

            for (s in stringSplitter) {
                val i = Integer.parseInt(s)
                if (type == -1) {
                    type = i
                    if (type != ROOT_ID && type != VIDEO_ID && type != AUDIO_ID && type != IMAGE_ID)
                        throw ContentDirectoryException(
                            ContentDirectoryErrorCode.NO_SUCH_OBJECT,
                            "Invalid type!"
                        )
                } else {
                    subtype.add(i)
                }
            }

            var container: Container? = null

            Timber.d("Browsing type $type")

            val rootContainer = BaseContainer(
                ROOT_ID.toString(),
                ROOT_ID.toString(),
                appName,
                appName,
                baseURL
            )

            val (imageContainer: Container?, allImageContainer: Container?) = populateImageContainer(
                rootContainer
            )

            var audioContainer: Container? = null

            var artistAudioContainer: Container? = null

            var albumAudioContainer: Container? = null

            var allAudioContainer: Container? = null

            if (sharedPref.getBoolean(CONTENT_DIRECTORY_AUDIO, true)) {
                audioContainer = BaseContainer(
                    AUDIO_ID.toString(),
                    ROOT_ID.toString(),
                    context.getString(R.string.audio),
                    appName,
                    baseURL
                )

                with(rootContainer) {
                    addContainer(audioContainer)
                    childCount += 1
                }

                with(audioContainer) {
                    artistAudioContainer = ArtistContainer(
                        ARTIST_ID.toString(),
                        AUDIO_ID.toString(),
                        context.getString(R.string.artist),
                        appName,
                        baseURL,
                        context,
                        cache
                    )

                    addContainer(artistAudioContainer)
                    childCount += 1

                    albumAudioContainer = AlbumContainer(
                        ALBUM_ID.toString(),
                        AUDIO_ID.toString(),
                        context.getString(R.string.album),
                        appName,
                        baseURL,
                        context,
                        null,
                        cache = cache
                    )

                    addContainer(albumAudioContainer)
                    childCount += 1

                    allAudioContainer = AudioContainer(
                        ALL_ID.toString(),
                        AUDIO_ID.toString(),
                        context.getString(R.string.all),
                        appName,
                        baseURL,
                        context,
                        null,
                        null,
                        cache = cache
                    )

                    addContainer(allAudioContainer)
                    childCount += 1
                }
            }

            val (videoContainer: Container?, allVideoContainer: Container?) =
                populateVideoContainer(rootContainer)

            if (subtype.isEmpty()) {
                container = when (type) {
                    ROOT_ID -> rootContainer
                    AUDIO_ID -> audioContainer
                    VIDEO_ID -> videoContainer
                    IMAGE_ID -> imageContainer
                    else -> container
                }
            } else {
                when (type) {
                    VIDEO_ID -> if (subtype[0] == ALL_ID) {
                        Timber.d("Listing all videos...")
                        container = allVideoContainer
                    }

                    AUDIO_ID -> when {
                        subtype.size == 1 -> when {
                            subtype[0] == ARTIST_ID -> {
                                Timber.d("Listing all artists...")
                                container = artistAudioContainer
                            }

                            subtype[0] == ALBUM_ID -> {
                                Timber.d("Listing album of all artists...")
                                container = albumAudioContainer
                            }

                            subtype[0] == ALL_ID -> {
                                Timber.d("Listing all songs...")
                                container = allAudioContainer
                            }
                        }
                        subtype.size == 2 && subtype[0] == ARTIST_ID -> {
                            val artistId = subtype[1].toString()
                            val parentId = "$AUDIO_ID$SEPARATOR${subtype[0]}"
                            Timber.d("Listing album of artist $artistId")

                            container = AlbumContainer(
                                artistId,
                                parentId,
                                "",
                                appName,
                                baseURL,
                                context,
                                artistId,
                                cache = cache
                            )
                        }
                        subtype.size == 2 && subtype[0] == ALBUM_ID -> {
                            val albumId = subtype[1].toString()
                            val parentId = "$AUDIO_ID$SEPARATOR${subtype[0]}"
                            Timber.d("Listing song of album $albumId")

                            container = AudioContainer(
                                albumId,
                                parentId,
                                "",
                                appName,
                                baseURL,
                                context,
                                null,
                                albumId,
                                cache = cache
                            )
                        }
                        subtype.size == 3 && subtype[0] == ARTIST_ID -> {
                            val albumId = subtype[2].toString()
                            val parentId =
                                "$AUDIO_ID$SEPARATOR${subtype[0]}$SEPARATOR${subtype[1]}"
                            Timber.d(
                                "Listing song of album %s for artist %s",
                                albumId,
                                subtype[1]
                            )

                            container = AudioContainer(
                                albumId,
                                parentId,
                                "",
                                appName,
                                baseURL,
                                context,
                                null,
                                albumId,
                                cache = cache
                            )
                        }
                    }

                    IMAGE_ID -> if (subtype[0] == ALL_ID) {
                        Timber.d("Listing all images...")
                        container = allImageContainer
                    }
                }
            }

            container?.let {
                Timber.d("List container...")
                val didl = DIDLContent()

                // Get container first
                for (c in it.containers)
                    didl.addContainer(c)

                Timber.d("List item...")

                // Then get item
                for (i in it.items)
                    didl.addItem(i)

                Timber.d("Return result...")

                val count = it.childCount

                Timber.d("Child count: $count")
                val answer: String

                try {
                    answer = DIDLParser().generate(didl)
                } catch (ex: Exception) {
                    throw ContentDirectoryException(
                        ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString()
                    )
                }

                Timber.d("Answer : $answer")

                return BrowseResult(answer, count.toLong(), count.toLong())
            }
        } catch (ex: Exception) {
            throw ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString())
        }

        Timber.e("No container for: $objectID")
        throw ContentDirectoryException(ContentDirectoryErrorCode.NO_SUCH_OBJECT)
    }

    private fun populateImageContainer(rootContainer: BaseContainer): Pair<Container?, Container?> {
        var imageContainer: Container? = null
        var allImageContainer: Container? = null

        if (sharedPref.getBoolean(CONTENT_DIRECTORY_IMAGE, true)) {
            imageContainer = BaseContainer(
                IMAGE_ID.toString(),
                ROOT_ID.toString(),
                context.getString(R.string.images),
                appName,
                baseURL
            )
            rootContainer.addContainer(imageContainer)
            rootContainer.childCount = rootContainer.childCount + 1

            allImageContainer = ImageContainer(
                ALL_ID.toString(),
                IMAGE_ID.toString(),
                context.getString(R.string.all),
                appName,
                baseURL,
                context,
                cache = cache
            )
            imageContainer.addContainer(allImageContainer)
            imageContainer.childCount = imageContainer.childCount + 1
        }
        return Pair(imageContainer, allImageContainer)
    }

    private fun populateVideoContainer(rootContainer: BaseContainer): Pair<Container?, Container?> {
        var videoContainer: Container? = null
        var allVideoContainer: Container? = null

        if (sharedPref.getBoolean(CONTENT_DIRECTORY_VIDEO, true)) {
            videoContainer = BaseContainer(
                VIDEO_ID.toString(),
                ROOT_ID.toString(),
                context.getString(R.string.videos),
                appName,
                baseURL
            )

            with(rootContainer) {
                addContainer(videoContainer)
                childCount = rootContainer.childCount + 1
            }

            allVideoContainer = VideoContainer(
                ALL_ID.toString(),
                VIDEO_ID.toString(),
                context.getString(R.string.all),
                appName,
                baseURL,
                context,
                cache = cache
            )

            with(videoContainer) {
                addContainer(allVideoContainer)
                childCount = videoContainer.childCount + 1
            }
        }

        return Pair(videoContainer, allVideoContainer)
    }

    companion object {
        const val SEPARATOR = '$'

        // Type
        const val ROOT_ID = 0
        const val VIDEO_ID = 1
        const val AUDIO_ID = 2
        const val IMAGE_ID = 3

        // Type subfolder
        const val ALL_ID = 0
        const val FOLDER_ID = 1
        const val ARTIST_ID = 2
        const val ALBUM_ID = 3

        // Prefix item
        const val VIDEO_PREFIX = "v-"
        const val AUDIO_PREFIX = "a-"
        const val IMAGE_PREFIX = "i-"
        const val DIRECTORY_PREFIX = "d-"

        fun isRoot(parentId: String?) =
            parentId?.compareTo(ROOT_ID.toString()) == 0

        fun getLocalDevice(
            serviceResourceProvider: LocalServiceResourceProvider,
            context: Context,
            contentCache: ContentCache
        ): LocalDevice {
            val details = DeviceDetails(
                serviceResourceProvider.settingContentDirectoryName,

                ManufacturerDetails(
                    serviceResourceProvider.appName,
                    serviceResourceProvider.appUrl
                ),
                ModelDetails(
                    serviceResourceProvider.appName,
                    serviceResourceProvider.appUrl
                ),
                serviceResourceProvider.appName,
                serviceResourceProvider.appVersion
            )

            val validationErrors = details.validate()

            for (error in validationErrors) {
                Timber.e("Validation pb for property %s", error.propertyName)
                Timber.e("Error is %s", error.message)
            }

            val type = UDADeviceType("MediaServer", 1)

            return LocalDevice(
                DeviceIdentity(UDN.valueOf(UUID(0, 10).toString())),
                type,
                details,
                getLocalService(context, contentCache)
            )
        }

        private fun getLocalService(
            context: Context,
            contentCache: ContentCache
        ) = (AnnotationLocalServiceBinder()
            .read(ContentDirectoryService::class.java) as LocalService<ContentDirectoryService>)
            .apply {
                manager = DefaultServiceManager(this, ContentDirectoryService::class.java).apply {
                    (implementation as ContentDirectoryService).let { service ->
                        service.context = context
                        service.baseURL = "${getLocalIpAddress(context).hostAddress}:$PORT"
                        service.sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                        service.cache = contentCache
                    }
                }
            }
    }
}
