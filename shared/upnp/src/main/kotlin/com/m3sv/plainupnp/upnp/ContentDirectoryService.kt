package com.m3sv.plainupnp.upnp

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.m3sv.plainupnp.core.persistence.Database
import com.m3sv.plainupnp.upnp.mediacontainers.*
import com.m3sv.plainupnp.upnp.util.*
import comm3svplainupnpcorepersistence.DirectoryCache
import comm3svplainupnpcorepersistence.FileCache
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
import java.security.SecureRandom
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.math.abs

class ContentDirectoryService : AbstractContentDirectoryService() {
    lateinit var context: Context
    lateinit var baseURL: String
    lateinit var sharedPref: SharedPreferences
    lateinit var database: Database

    private val appName by lazy(NONE) { context.getString(R.string.app_name) }
    private val containerRegistry: MutableMap<Long, BaseContainer> = mutableMapOf()

    override fun browse(
        objectID: String,
        browseFlag: BrowseFlag,
        filter: String,
        firstResult: Long,
        maxResults: Long,
        orderby: Array<SortCriterion>,
    ): BrowseResult = runBlocking {
        try {
            var root = -1L

            val subtype = objectID
                .split(SEPARATOR)
                .map(String::toLong)
                .map {
                    if (root == -1L) {
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
                ).addToRegistry()
            }

            val rootContainer: BaseContainer = requireNotNull(containerRegistry[ROOT_ID])

            val jobs = mutableListOf<Job>()

            if (isImagesEnabled && containerRegistry[IMAGE_ID] == null) {
                jobs += launch(Dispatchers.IO) {
                    getRootImagesContainer()
                        .also(rootContainer::addContainer)
                        .addToRegistry()
                }
            }

            if (isAudioEnabled && containerRegistry[AUDIO_ID] == null) {
                jobs += launch(Dispatchers.IO) {
                    getRootAudioContainer(rootContainer).addToRegistry()
                }
            }

            if (isVideoEnabled && containerRegistry[VIDEO_ID] == null) {
                jobs += launch(Dispatchers.IO) {
                    getRootVideoContainer(rootContainer).addToRegistry()
                }
            }

            jobs += launch(Dispatchers.IO) { getUserSelectedContainer(rootContainer as Container) }

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
            container.addToRegistry()
        }

        Container(
            IMAGE_BY_FOLDER.toString(),
            rootContainer.id,
            context.getString(R.string.by_folder),
            appName
        ).also { container ->
            rootContainer.addContainer(container)
            container.addToRegistry()

            val initialId: Long = 1_000_000
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
                container.addToRegistry()
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
                container.addToRegistry()
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
                container.addToRegistry()
            }

            Container(
                AUDIO_BY_FOLDER.toString(),
                id,
                context.getString(R.string.by_folder),
                appName
            ).also { container ->
                addContainer(container)
                container.addToRegistry()

                val initialId: Long = 2_000_000
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
                container.addToRegistry()
            }

            Container(
                VIDEO_BY_FOLDER.toString(),
                id,
                context.getString(R.string.by_folder),
                appName
            ).also { container ->
                addContainer(container)
                container.addToRegistry()

                val initialId: Long = 3_000_000

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

    private fun getUserSelectedContainer(rootContainer: Container) {
        context
            .contentResolver
            .persistedUriPermissions
            .map { it.uri }
            .mapNotNull { DocumentFile.fromTreeUri(context, it) }
            .forEach { documentFile ->
                when {
                    documentFile.isDirectory -> handleDirectory(documentFile, rootContainer)
                    documentFile.isFile -> queryUri(documentFile.uri, rootContainer)

                    documentFile.isVirtual -> {
                        // TODO not supported yet
                    }
                }
            }
    }

    private fun handleDirectory(
        documentFile: DocumentFile,
        rootContainer: Container,
    ) {
        if (isContainerCached(documentFile.uri)) {
            return
        }

        queryOrCreateContainer(documentFile.uri, rootContainer)?.let { parentContainer ->
            documentFile.listFiles().forEach {
                when {
                    it.isFile -> queryUri(it.uri, parentContainer)
                    it.isDirectory -> handleDirectory(it, parentContainer)
                }
            }
        }
    }

    private fun isContainerCached(uri: Uri): Boolean {
        val cachedEntry =
            database.directoryCacheQueries.selectByUri(uri.toString()).executeAsOneOrNull() ?: return false
        return containerRegistry[cachedEntry._id] != null
    }

    private fun queryOrCreateContainer(uri: Uri, parentContainer: Container): Container? {
        return (database.directoryCacheQueries.selectByUri(uri.toString()).executeAsOneOrNull()?.let { cachedValue ->
            createContainer(cachedValue._id, parentContainer.rawId, cachedValue.name)
        } ?: createFolderContainer(uri, parentContainer.rawId))?.also {
            parentContainer.addContainer(it)
            it.addToRegistry()
        }
    }

    private fun createFolderContainer(uri: Uri, parentId: String): Container? = context
        .contentResolver
        .query(uri,
            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)

            if (cursor.moveToFirst()) {
                val id = getRandomId()
                val name = cursor.getString(nameColumn)

                database.directoryCacheQueries.insertEntry(DirectoryCache(id, uri.toString(), name))
                createContainer(id, parentId, name)
            } else
                null
        }

    private fun createContainer(
        id: Long,
        parentId: String,
        name: String?,
    ) = Container(id.toString(), parentId, "$USER_DEFINED_PREFIX$name", null)

    private fun queryUri(
        uri: Uri,
        parentContainer: Container,
    ) {
        getCachedFile(uri)?.apply {
            val id = "$TREE_PREFIX$_id"
            when {
                mime.startsWith("image") -> {
                    parentContainer.addImageItem(
                        baseURL = baseURL,
                        id = id,
                        name = name ?: "",
                        mime = mime,
                        width = width ?: 0L,
                        height = height ?: 0L,
                        size = size
                    )
                }
                mime.startsWith("audio") -> {
                    parentContainer.addAudioItem(
                        baseURL = baseURL,
                        id = id,
                        name = name ?: "",
                        mime = mime,
                        width = width ?: 0L,
                        height = height ?: 0L,
                        size = size,
                        duration = duration ?: 0L,
                        album = album ?: "",
                        creator = creator ?: "")
                }
                mime.startsWith("video") -> {
                    parentContainer.addVideoItem(
                        baseURL = baseURL,
                        id = id,
                        name = name ?: "",
                        mime = mime,
                        width = width ?: 0L,
                        height = height ?: 0L,
                        size = size,
                        duration = duration ?: 0L
                    )
                }
            }
        } ?: context
            .contentResolver
            .query(uri,
                arrayOf(
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.MIME_TYPE
                ), null, null, null)
            ?.use { cursor ->
                val mimeTypeColumn = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = getRandomId()
                    val mimeType = cursor.getString(mimeTypeColumn)
                    when {
                        mimeType.startsWith("image") -> context.contentResolver.queryImages(uri) { _, title, _, size, width, height ->
                            database
                                .fileCacheQueries
                                .insertEntry(
                                    FileCache(
                                        _id = id,
                                        uri = uri.toString(),
                                        mime = mimeType,
                                        name = title,
                                        width = width,
                                        height = height,
                                        duration = null,
                                        size = size,
                                        creator = null,
                                        album = null
                                    )
                                )

                            parentContainer.addImageItem(
                                baseURL = baseURL,
                                id = "$TREE_PREFIX$id",
                                name = title,
                                mime = mimeType,
                                width = width,
                                height = height,
                                size = size)

                        }

                        mimeType.startsWith("video") -> context.contentResolver.queryVideos(uri) { _, title, creator, _, size, duration, width, height ->
                            database
                                .fileCacheQueries
                                .insertEntry(
                                    FileCache(
                                        _id = id,
                                        uri = uri.toString(),
                                        mime = mimeType,
                                        name = title,
                                        width = width,
                                        height = height,
                                        duration = null,
                                        size = size,
                                        creator = null,
                                        album = null
                                    )
                                )

                            parentContainer.addVideoItem(
                                baseURL = baseURL,
                                id = "$TREE_PREFIX$id",
                                name = title,
                                mime = mimeType,
                                width = width,
                                height = height,
                                size = size,
                                duration = duration
                            )
                        }

                        mimeType.startsWith("audio") -> context.contentResolver.queryAudio(uri) { _, title, creator, _, size, duration, width, height, artist, album ->
                            database
                                .fileCacheQueries
                                .insertEntry(
                                    FileCache(
                                        _id = id,
                                        uri = uri.toString(),
                                        mime = mimeType,
                                        name = title,
                                        width = width,
                                        height = height,
                                        duration = null,
                                        size = size,
                                        creator = null,
                                        album = null
                                    )
                                )

                            parentContainer.addAudioItem(
                                baseURL = baseURL,
                                id = "$TREE_PREFIX$id",
                                name = title,
                                mime = mimeType,
                                width = width,
                                height = height,
                                size = size,
                                duration = duration,
                                album = album ?: "",
                                creator = creator ?: ""
                            )
                        }
                    }
                }
            }
    }

    private fun getCachedFile(uri: Uri): FileCache? {
        return database.fileCacheQueries.selectByUri(uri.toString()).executeAsOneOrNull()
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
        initialId: Long,
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
                ).apply { addToRegistry() }

                rootContainer.addContainer(childContainer)

                populateFromMap(childContainer, kv.value as Map<String, Map<String, Any>>)
            }
        }

        populateFromMap(rootContainer, folders)
    }

    private fun BaseContainer.addToRegistry() {
        containerRegistry[rawId.toLong()] = this
    }

    private val isImagesEnabled
        get() = sharedPref.getBoolean(CONTENT_DIRECTORY_IMAGE, true)

    private val isAudioEnabled
        get() = sharedPref.getBoolean(CONTENT_DIRECTORY_AUDIO, true)

    private val isVideoEnabled
        get() = sharedPref.getBoolean(CONTENT_DIRECTORY_VIDEO, true)

    companion object {
        const val USER_DEFINED_PREFIX = "USER_DEFINED_"
        const val SEPARATOR = '$'

        // Type
        const val ROOT_ID: Long = 0
        const val IMAGE_ID: Long = 1
        const val AUDIO_ID: Long = 2
        const val VIDEO_ID: Long = 3

        // Type subfolder
        const val ALL_IMAGE: Long = 10
        const val ALL_VIDEO: Long = 20
        const val ALL_AUDIO: Long = 30

        const val IMAGE_BY_FOLDER: Long = 100
        const val VIDEO_BY_FOLDER: Long = 200
        const val AUDIO_BY_FOLDER: Long = 300

        const val ALL_ARTISTS: Long = 301
        const val ALL_ALBUMS: Long = 302

        // Prefix item
        const val VIDEO_PREFIX = "v-"
        const val AUDIO_PREFIX = "a-"
        const val IMAGE_PREFIX = "i-"
        const val TREE_PREFIX = "t-"
        private val random = SecureRandom()
        private fun getRandomId() = abs(random.nextLong())

        private val noSuchObject =
            ContentDirectoryException(ContentDirectoryErrorCode.NO_SUCH_OBJECT)

        fun isRoot(parentId: String?) =
            parentId?.compareTo(ROOT_ID.toString()) == 0
    }
}
