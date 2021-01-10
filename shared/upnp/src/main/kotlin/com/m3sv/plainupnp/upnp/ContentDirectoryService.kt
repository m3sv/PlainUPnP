package com.m3sv.plainupnp.upnp

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore
import com.m3sv.plainupnp.upnp.mediacontainers.*
import com.m3sv.plainupnp.upnp.util.CONTENT_DIRECTORY_AUDIO
import com.m3sv.plainupnp.upnp.util.CONTENT_DIRECTORY_IMAGE
import com.m3sv.plainupnp.upnp.util.CONTENT_DIRECTORY_VIDEO
import kotlinx.coroutines.*
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException
import org.fourthline.cling.support.contentdirectory.DIDLParser
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.BrowseResult
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.SortCriterion
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE

class ContentDirectoryService : AbstractContentDirectoryService() {
    lateinit var context: Context
    lateinit var baseURL: String
    lateinit var sharedPref: SharedPreferences

    private val appName by lazy(NONE) { context.getString(R.string.app_name) }
    private val containerRegistry: MutableMap<Int, BaseContainer> = mutableMapOf()

    override fun browse(
        objectID: String,
        browseFlag: BrowseFlag,
        filter: String,
        firstResult: Long,
        maxResults: Long,
        orderby: Array<SortCriterion>,
    ): BrowseResult = runBlocking {
        try {
            var root = -1

            val subtype = objectID
                .split(SEPARATOR)
                .map(Integer::parseInt)
                .map {
                    if (root == -1) {
                        root = it

                        if (root != ROOT_ID
                            && root != VIDEO_ID
                            && root != AUDIO_ID
                            && root != IMAGE_ID
                            && root !in containerRegistry.keys
                        ) {
                            throw ContentDirectoryException(
                                ContentDirectoryErrorCode.NO_SUCH_OBJECT,
                                "Invalid type!"
                            )
                        }
                    }

                    it
                }
                    // drop 0 (HOME)
                .drop(1)

            Timber.d("Browsing type $root")
            if (containerRegistry[ROOT_ID] == null) {
                Container(
                    ROOT_ID.toString(),
                    ROOT_ID.toString(),
                    appName,
                    appName
                ).addToRegistry(ROOT_ID)
            }

            val rootContainer: BaseContainer = requireNotNull(containerRegistry[ROOT_ID])

            val jobs = mutableListOf<Job>()

            if (isImagesEnabled && containerRegistry[IMAGE_ID] == null) {
                jobs += launch(Dispatchers.IO) {
                    getRootImagesContainer()
                        .also(rootContainer::addContainer)
                        .addToRegistry(IMAGE_ID)
                }
            }

            if (isAudioEnabled && containerRegistry[AUDIO_ID] == null) {
                jobs += launch(Dispatchers.IO) {
                    getRootAudioContainer(rootContainer).addToRegistry(AUDIO_ID)
                }
            }

            if (isVideoEnabled && containerRegistry[VIDEO_ID] == null) {
                jobs += launch(Dispatchers.IO) {
                    getRootVideoContainer(rootContainer).addToRegistry(VIDEO_ID)
                }
            }

            jobs.joinAll()

            val container: BaseContainer = if (subtype.isEmpty()) {
                containerRegistry[root] ?: throw noSuchObject
            } else {
                when (root) {
                    VIDEO_ID -> containerRegistry[subtype[0]] ?: throw noSuchObject
                    AUDIO_ID -> when {
                        subtype.size == 1 -> containerRegistry[subtype[0]] ?: throw noSuchObject
                        subtype.size == 2 && subtype[0] == ALL_ARTISTS -> {
                            val artistId = subtype[1].toString()
                            val parentId = "$AUDIO_ID$SEPARATOR${subtype[0]}"
                            Timber.d("Listing album of artist $artistId")

                            AlbumContainer(
                                artistId,
                                parentId,
                                "",
                                appName,
                                baseURL,
                                context.contentResolver,
                                artistId = artistId
                            )
                        }
                        subtype.size == 2 && subtype[0] == ALL_ALBUMS -> {
                            val albumId = subtype[1].toString()
                            val parentId = "$AUDIO_ID$SEPARATOR${subtype[0]}"
                            Timber.d("Listing song of album $albumId")

                            getAlbumContainer(albumId, parentId)
                        }
                        subtype.size == 3 && subtype[0] == ALL_ARTISTS -> {
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

                    IMAGE_ID -> containerRegistry[subtype[0]] ?: throw noSuchObject
                    else -> containerRegistry[subtype[0]] ?: throw noSuchObject
                }
            }

            getBrowseResult(container)
        } catch (ex: Exception) {
            Timber.e(ex)
            throw ContentDirectoryException(
                ContentDirectoryErrorCode.CANNOT_PROCESS,
                ex.toString()
            )
        }
    }

    private fun getBrowseResult(container: BaseContainer): BrowseResult {
        Timber.d("List container...")

        val didl = DIDLContent().apply {
            listOf(
                LinkedHashSet(container.containers),
                LinkedHashSet(container.items)
            ).flatten().forEach { addObject(it) }
        }

        val count = didl.count

        Timber.d("Child count: $count")

        val answer: String

        try {
            answer = DIDLParser().generate(didl)
        } catch (ex: Exception) {
            throw ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString())
        }

        return BrowseResult(answer, count, count)
    }

    private fun getRootImagesContainer(): BaseContainer {
        val rootContainer = Container(
            IMAGE_ID.toString(),
            ROOT_ID.toString(),
            context.getString(R.string.images),
            appName
        )

        AllImagesContainer(
            id = ALL_IMAGE.toString(),
            parentID = IMAGE_ID.toString(),
            title = context.getString(R.string.all),
            creator = appName,
            baseUrl = baseURL,
            contentResolver = context.contentResolver
        ).also { container ->
            rootContainer.addContainer(container)
            container.addToRegistry(ALL_IMAGE)
        }

        Container(
            IMAGE_BY_FOLDER.toString(),
            rootContainer.id,
            context.getString(R.string.by_folder),
            appName
        ).also { container ->
            rootContainer.addContainer(container)
            container.addToRegistry(IMAGE_BY_FOLDER)

            val initialId = 1_000_000
            val column = ImageDirectoryContainer.IMAGE_DATA_PATH
            val externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            generateContainerStructure(initialId,
                column,
                container,
                externalContentUri) { id, parentID, title, creator, baseUrl, contentDirectory, contentResolver ->
                ImageDirectoryContainer(
                    id = id,
                    parentID = parentID,
                    title = title,
                    creator = creator,
                    baseUrl = baseUrl,
                    directory = contentDirectory,
                    contentResolver = contentResolver
                )
            }
        }

        return rootContainer
    }

    private fun getRootAudioContainer(rootContainer: BaseContainer): BaseContainer =
        Container(
            AUDIO_ID.toString(),
            ROOT_ID.toString(),
            context.getString(R.string.audio),
            appName
        ).apply {
            rootContainer.addContainer(this)

            AllAudioContainer(
                ALL_AUDIO.toString(),
                AUDIO_ID.toString(),
                context.getString(R.string.all),
                appName,
                baseUrl = baseURL,
                contentResolver = context.contentResolver,
                albumId = null,
                artist = null
            ).also { container ->
                addContainer(container)
                container.addToRegistry(ALL_AUDIO)
            }

            ArtistContainer(
                ALL_ARTISTS.toString(),
                AUDIO_ID.toString(),
                context.getString(R.string.artist),
                appName,
                baseURL,
                context.contentResolver
            ).also { container ->
                addContainer(container)
                container.addToRegistry(ALL_ARTISTS)
            }

            AlbumContainer(
                ALL_ALBUMS.toString(),
                AUDIO_ID.toString(),
                context.getString(R.string.album),
                appName,
                baseURL,
                context.contentResolver,
                null
            ).also { container ->
                addContainer(container)
                container.addToRegistry(ALL_ALBUMS)
            }

            Container(
                AUDIO_BY_FOLDER.toString(),
                id,
                context.getString(R.string.by_folder),
                appName
            ).also { container ->
                addContainer(container)
                container.addToRegistry(AUDIO_BY_FOLDER)

                val initialId = 2_000_000
                val column = AudioDirectoryContainer.AUDIO_DATA_PATH
                val externalContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

                generateContainerStructure(initialId,
                    column,
                    container,
                    externalContentUri) { id, parentID, title, creator, baseUrl, contentDirectory, contentResolver ->
                    AudioDirectoryContainer(
                        id = id,
                        parentID = parentID,
                        title = title,
                        creator = creator,
                        baseUrl = baseUrl,
                        directory = contentDirectory,
                        contentResolver = contentResolver
                    )
                }
            }
        }

    private fun getRootVideoContainer(rootContainer: BaseContainer): BaseContainer =
        Container(
            VIDEO_ID.toString(),
            ROOT_ID.toString(),
            context.getString(R.string.videos),
            appName
        ).apply {
            rootContainer.addContainer(this)

            AllVideoContainer(
                ALL_VIDEO.toString(),
                VIDEO_ID.toString(),
                context.getString(R.string.all),
                appName,
                baseURL,
                contentResolver = context.contentResolver
            ).also { container ->
                addContainer(container)
                container.addToRegistry(ALL_VIDEO)
            }

            Container(
                VIDEO_BY_FOLDER.toString(),
                id,
                context.getString(R.string.by_folder),
                appName
            ).also { container ->
                addContainer(container)
                container.addToRegistry(VIDEO_BY_FOLDER)

                val initialId = 3_000_000

                val column = VideoDirectoryContainer.VIDEO_DATA_PATH
                val externalContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                generateContainerStructure(initialId,
                    column,
                    container,
                    externalContentUri) { id, parentID, title, creator, baseUrl, contentDirectory, contentResolver ->
                    VideoDirectoryContainer(
                        id = id,
                        parentID = parentID,
                        title = title,
                        creator = creator,
                        baseUrl = baseUrl,
                        directory = contentDirectory,
                        contentResolver = contentResolver
                    )
                }
            }
        }

    private fun getAlbumContainer(
        albumId: String,
        parentId: String,
    ): BaseContainer = AllAudioContainer(
        albumId,
        parentId,
        "",
        appName,
        baseUrl = baseURL,
        contentResolver = context.contentResolver,
        albumId = albumId,
        artist = null
    )

    private fun generateContainerStructure(
        initialId: Int,
        column: String,
        rootContainer: BaseContainer,
        externalContentUri: Uri,
        childContainerBuilder: (
            id: String,
            parentID: String?,
            title: String,
            creator: String,
            baseUrl: String,
            contentDirectory: ContentDirectory,
            contentResolver: ContentResolver,
        ) -> BaseContainer,
    ) {
        var initialId = initialId
        val folders: MutableMap<String, Map<String, Any>> = mutableMapOf()
        buildSet<String> {
            context.contentResolver.query(
                externalContentUri,
                arrayOf(column),
                null,
                null,
                null
            )?.use { cursor ->
                val pathColumn = cursor.getColumnIndexOrThrow(column)

                while (cursor.moveToNext()) {
                    cursor
                        .getString(pathColumn)
                        .let { path ->
                            when {
                                path.startsWith("/") -> path.drop(1)
                                path.endsWith("/") -> path.dropLast(1)
                                else -> path
                            }
                        }.also(::add)
                }
            }
        }.map { it.split("/") }.forEach {
            lateinit var map: MutableMap<String, Map<String, Any>>

            it.forEachIndexed { index, s ->
                if (index == 0) {
                    if (folders[s] == null)
                        folders[s] = mutableMapOf<String, Map<String, Any>>()

                    map = folders[s] as MutableMap<String, Map<String, Any>>
                } else {
                    if (map[s] == null)
                        map[s] = mutableMapOf<String, Map<String, Any>>()

                    map = map[s] as MutableMap<String, Map<String, Any>>
                }
            }
        }


        fun populateFromMap(rootContainer: BaseContainer, map: Map<String, Map<String, Any>>) {
            map.forEach { kv ->
                val id = initialId++

                val childContainer = childContainerBuilder(
                    id.toString(),
                    rootContainer.rawId,
                    kv.key,
                    appName,
                    baseURL,
                    ContentDirectory(kv.key),
                    context.contentResolver
                ).apply { addToRegistry(id) }

                rootContainer.addContainer(childContainer)

                populateFromMap(childContainer, kv.value as Map<String, Map<String, Any>>)
            }
        }

        populateFromMap(rootContainer, folders)
    }

    private fun BaseContainer.addToRegistry(key: Int) {
        containerRegistry[key] = this
    }

    private val isImagesEnabled
        get() = sharedPref.getBoolean(CONTENT_DIRECTORY_IMAGE, true)

    private val isAudioEnabled
        get() = sharedPref.getBoolean(CONTENT_DIRECTORY_AUDIO, true)

    private val isVideoEnabled
        get() = sharedPref.getBoolean(CONTENT_DIRECTORY_VIDEO, true)

    companion object {
        const val SEPARATOR = '$'

        // Type
        const val ROOT_ID = 0
        const val IMAGE_ID = 1
        const val AUDIO_ID = 2
        const val VIDEO_ID = 3

        // Type subfolder
        const val ALL_IMAGE = 10
        const val ALL_VIDEO = 20
        const val ALL_AUDIO = 30

        const val IMAGE_BY_FOLDER = 100
        const val VIDEO_BY_FOLDER = 200
        const val AUDIO_BY_FOLDER = 300

        const val ALL_ARTISTS = 301
        const val ALL_ALBUMS = 302

        // Prefix item
        const val VIDEO_PREFIX = "v-"
        const val AUDIO_PREFIX = "a-"
        const val IMAGE_PREFIX = "i-"

        private val noSuchObject =
            ContentDirectoryException(ContentDirectoryErrorCode.NO_SUCH_OBJECT)

        fun isRoot(parentId: String?) =
            parentId?.compareTo(ROOT_ID.toString()) == 0
    }
}
