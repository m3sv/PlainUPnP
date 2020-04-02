package com.m3sv.plainupnp.upnp

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.m3sv.plainupnp.common.ContentCache
import com.m3sv.plainupnp.upnp.mediacontainers.*
import kotlinx.coroutines.runBlocking
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

class ContentDirectoryService : AbstractContentDirectoryService() {

    lateinit var context: Context

    lateinit var baseURL: String

    lateinit var sharedPref: SharedPreferences

    lateinit var cache: ContentCache

    private val appName by lazy(mode = LazyThreadSafetyMode.NONE) { context.getString(R.string.app_name) }

    private val stringSplitter = TextUtils.SimpleStringSplitter(SEPARATOR)

    @Throws(ContentDirectoryException::class)
    override fun browse(
        objectID: String,
        browseFlag: BrowseFlag,
        filter: String,
        firstResult: Long,
        maxResults: Long,
        orderby: Array<SortCriterion>
    ): BrowseResult = runBlocking {
        try {
            var type = -1
            val subtype = mutableListOf<Int>()

            stringSplitter.setString(objectID)
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

            Timber.d("Browsing type $type")

            val rootContainer = BaseContainer(
                ROOT_ID.toString(),
                ROOT_ID.toString(),
                appName,
                appName,
                baseURL
            )

            val rootImagesContainer: Container? = getRootImagesContainer(rootContainer)
            val rootAudioContainer: Container? = getRootAudioContainer(rootContainer)
            val rootVideoContainer: Container? = getRootVideoContainer(rootContainer)

            val container: Container? = if (subtype.isEmpty()) {
                when (type) {
                    ROOT_ID -> rootContainer
                    AUDIO_ID -> rootAudioContainer
                    VIDEO_ID -> rootVideoContainer
                    IMAGE_ID -> rootImagesContainer
                    else -> throw noSuchObject
                }
            } else {
                when (type) {
                    VIDEO_ID ->
                        if (subtype[0] == ALL_ID) {
                            getAllVideosContainer()
                        } else
                            throw noSuchObject

                    AUDIO_ID -> when {
                        subtype.size == 1 -> when (subtype[0]) {
                            ARTIST_ID -> getAllArtistsContainer()
                            ALBUM_ID -> getAllAlbumsContainer()
                            ALL_ID -> getAllAudioContainer()
                            else -> throw noSuchObject
                        }

                        subtype.size == 2 && subtype[0] == ARTIST_ID -> {
                            val artistId = subtype[1].toString()
                            val parentId = "$AUDIO_ID$SEPARATOR${subtype[0]}"
                            Timber.d("Listing album of artist $artistId")

                            getArtistContainer(artistId, parentId)
                        }

                        subtype.size == 2 && subtype[0] == ALBUM_ID -> {
                            val albumId = subtype[1].toString()
                            val parentId = "$AUDIO_ID$SEPARATOR${subtype[0]}"
                            Timber.d("Listing song of album $albumId")

                            getAlbumContainer(albumId, parentId)
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

                            getAlbumContainer(albumId, parentId)
                        }
                        else -> throw noSuchObject
                    }

                    IMAGE_ID ->
                        if (subtype[0] == ALL_ID)
                            getAllImagesContainer()
                        else
                            throw noSuchObject

                    else -> throw noSuchObject
                }
            }

            if (container != null) {
                getBrowseResult(container)
            } else
                throw noSuchObject
        } catch (ex: Exception) {
            throw ContentDirectoryException(
                ContentDirectoryErrorCode.CANNOT_PROCESS,
                ex.toString()
            )
        }
    }

    private fun getRootVideoContainer(rootContainer: BaseContainer): BaseContainer? =
        if (videoEnabled) {
            Timber.d("Fetch videos")
            BaseContainer(
                VIDEO_ID.toString(),
                ROOT_ID.toString(),
                context.getString(R.string.videos),
                appName,
                baseURL
            ).apply {
                rootContainer.addContainer(this)
                addContainer(getAllVideosContainer())
            }
        } else null

    private fun getRootAudioContainer(rootContainer: BaseContainer): BaseContainer? =
        if (audioEnabled) {
            Timber.d("Fetch audio")
            BaseContainer(
                AUDIO_ID.toString(),
                ROOT_ID.toString(),
                context.getString(R.string.audio),
                appName,
                baseURL
            ).apply {
                rootContainer.addContainer(this)
                addContainer(getAllArtistsContainer())
                addContainer(getAllAlbumsContainer())
                addContainer(getAllAudioContainer())
            }
        } else null

    private fun getRootImagesContainer(rootContainer: BaseContainer): BaseContainer? =
        if (imagesEnabled) {
            Timber.d("Fetch images")
            BaseContainer(
                IMAGE_ID.toString(),
                ROOT_ID.toString(),
                context.getString(R.string.images),
                appName,
                baseURL
            ).apply {
                rootContainer.addContainer(this)
                addContainer(getAllImagesContainer())
            }
        } else null

    private fun getBrowseResult(container: Container): BrowseResult {
        Timber.d("List container...")
        val didl = DIDLContent()

        // Get container first
        for (c in container.containers)
            didl.addContainer(c)

        Timber.d("List item...")

        // Then get item
        for (i in container.items)
            didl.addItem(i)

        Timber.d("Return result...")

        val count = container.childCount

        Timber.d("Child count: $count")
        val answer: String

        try {
            answer = DIDLParser().generate(didl)
        } catch (ex: Exception) {
            throw ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString())
        }

        Timber.d("Answer: $answer")

        return BrowseResult(answer, count.toLong(), count.toLong())
    }

    private val noSuchObject = ContentDirectoryException(ContentDirectoryErrorCode.NO_SUCH_OBJECT)

    private fun getAlbumContainer(
        albumId: String,
        parentId: String
    ): AudioContainer = AudioContainer(
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

    private fun getArtistContainer(
        artistId: String,
        parentId: String
    ): AlbumContainer = AlbumContainer(
        artistId,
        parentId,
        "",
        appName,
        baseURL,
        context,
        artistId,
        cache = cache
    )

    private fun getAllVideosContainer(): VideoContainer =
        VideoContainer(
            ALL_ID.toString(),
            VIDEO_ID.toString(),
            context.getString(R.string.all),
            appName,
            baseURL,
            context,
            cache = cache
        )

    private fun getAllAudioContainer(): AudioContainer =
        AudioContainer(
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

    private fun getAllAlbumsContainer(): AlbumContainer =
        AlbumContainer(
            ALBUM_ID.toString(),
            AUDIO_ID.toString(),
            context.getString(R.string.album),
            appName,
            baseURL,
            context,
            null,
            cache = cache
        )

    private fun getAllArtistsContainer(): ArtistContainer =
        ArtistContainer(
            ARTIST_ID.toString(),
            AUDIO_ID.toString(),
            context.getString(R.string.artist),
            appName,
            baseURL,
            context,
            cache
        )

    private fun getAllImagesContainer(): ImageContainer =
        ImageContainer(
            ALL_ID.toString(),
            IMAGE_ID.toString(),
            context.getString(R.string.all),
            appName,
            baseURL,
            context,
            cache = cache
        )

    private val imagesEnabled
        get() = sharedPref.getBoolean(CONTENT_DIRECTORY_IMAGE, true)

    private val audioEnabled
        get() = sharedPref.getBoolean(CONTENT_DIRECTORY_AUDIO, true)

    private val videoEnabled
        get() = sharedPref.getBoolean(CONTENT_DIRECTORY_VIDEO, true)

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
    }
}
