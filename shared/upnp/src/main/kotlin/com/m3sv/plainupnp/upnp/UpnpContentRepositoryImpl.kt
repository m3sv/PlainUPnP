package com.m3sv.plainupnp.upnp

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.m3sv.plainupnp.ContentRepository
import com.m3sv.plainupnp.common.preferences.PreferencesRepository
import com.m3sv.plainupnp.core.persistence.Database
import com.m3sv.plainupnp.upnp.mediacontainers.*
import com.m3sv.plainupnp.upnp.util.*
import comm3svplainupnpcorepersistence.DirectoryCache
import comm3svplainupnpcorepersistence.FileCache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

typealias ContentCache = MutableMap<Long, BaseContainer>

sealed class ContentUpdateState {
    object Loading : ContentUpdateState()
    data class Ready(val data: ContentCache) : ContentUpdateState()
}

@Singleton
class UpnpContentRepositoryImpl @Inject constructor(
    private val application: Application,
    private val database: Database,
    private val preferencesRepository: PreferencesRepository,
) : CoroutineScope, ContentRepository {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO

    val containerRegistry: MutableMap<Long, BaseContainer> = mutableMapOf()

    private val random = SecureRandom()

    private val randomId
        get() = abs(random.nextLong())

    private val appName by lazy { application.getString(R.string.app_name) }
    private val baseUrl: String by lazy { "${getLocalIpAddress(application).hostAddress}:$PORT" }

    private val refreshInternal = MutableSharedFlow<Unit>()

    private val _updateState: MutableStateFlow<ContentUpdateState> =
        MutableStateFlow(ContentUpdateState.Ready(containerRegistry))

    val updateState: Flow<ContentUpdateState> = _updateState

    init {
        launch {
            refreshInternal.collect {
                Timber.d("Updating content")
                _updateState.value = ContentUpdateState.Loading
                refreshInternal()
                _updateState.value = ContentUpdateState.Ready(containerRegistry)
            }
        }

        launch {
            preferencesRepository
                .preferences
                .debounce(2000)
                .filter { it.refreshContent }
                .collect { refreshContent() }
        }
    }

    val init by lazy {
        runBlocking { refreshInternal() }
    }

    override fun refreshContent() {
        launch { refreshInternal.emit(Unit) }
    }

    private suspend fun refreshInternal() = coroutineScope {
        containerRegistry.clear()

        Container(
            ROOT_ID.toString(),
            ROOT_ID.toString(),
            appName,
            appName
        ).addToRegistry()

        val rootContainer: BaseContainer = requireNotNull(containerRegistry[ROOT_ID])

        val jobs = mutableListOf<Deferred<Unit>>()

        with(requireNotNull(preferencesRepository.preferences.value.preferences)) {
            if (enableImages) {
                jobs += async {
                    getRootImagesContainer()
                        .also(rootContainer::addContainer)
                        .addToRegistry()
                }
            }

            if (enableAudio) {
                jobs += async {
                    getRootAudioContainer(rootContainer).addToRegistry()
                }
            }

            if (enableVideos) {
                jobs += async {
                    getRootVideoContainer(rootContainer).addToRegistry()
                }
            }
        }

        jobs += async { getUserSelectedContainer(rootContainer as Container) }

        jobs.joinAll()
    }

    fun getAudioContainerForAlbum(
        albumId: String,
        parentId: String,
    ): BaseContainer = AllAudioContainer(
        id = albumId,
        parentID = parentId,
        title = "",
        creator = appName,
        baseUrl = baseUrl,
        contentResolver = application.contentResolver,
        albumId = albumId,
        artist = null
    )

    fun getAlbumContainerForArtist(
        artistId: String,
        parentId: String,
    ): AlbumContainer = AlbumContainer(
        artistId,
        parentId,
        "",
        appName,
        baseUrl,
        application.contentResolver,
        artistId = artistId
    )

    private fun BaseContainer.addToRegistry() {
        containerRegistry[rawId.toLong()] = this
    }

    private fun getRootImagesContainer(): BaseContainer {
        val rootContainer = Container(
            IMAGE_ID.toString(),
            ROOT_ID.toString(),
            application.getString(R.string.images),
            appName
        )

        AllImagesContainer(
            id = ALL_IMAGE.toString(),
            parentID = IMAGE_ID.toString(),
            title = application.getString(R.string.all),
            creator = appName,
            baseUrl = baseUrl,
            contentResolver = application.contentResolver
        ).also { container ->
            rootContainer.addContainer(container)
            container.addToRegistry()
        }

        Container(
            IMAGE_BY_FOLDER.toString(),
            rootContainer.id,
            application.getString(R.string.by_folder),
            appName
        ).also { container ->
            rootContainer.addContainer(container)
            container.addToRegistry()

            val column = ImageDirectoryContainer.IMAGE_DATA_PATH
            val externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            generateContainerStructure(
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
            application.getString(R.string.audio),
            appName
        ).apply {
            rootContainer.addContainer(this)

            AllAudioContainer(
                ALL_AUDIO.toString(),
                AUDIO_ID.toString(),
                application.getString(R.string.all),
                appName,
                baseUrl = baseUrl,
                contentResolver = application.contentResolver,
                albumId = null,
                artist = null
            ).also { container ->
                addContainer(container)
                container.addToRegistry()
            }

            ArtistContainer(
                ALL_ARTISTS.toString(),
                AUDIO_ID.toString(),
                application.getString(R.string.artist),
                appName,
                baseUrl,
                application.contentResolver
            ).also { container ->
                addContainer(container)
                container.addToRegistry()
            }

            AlbumContainer(
                ALL_ALBUMS.toString(),
                AUDIO_ID.toString(),
                application.getString(R.string.album),
                appName,
                baseUrl,
                application.contentResolver,
                null
            ).also { container ->
                addContainer(container)
                container.addToRegistry()
            }

            Container(
                AUDIO_BY_FOLDER.toString(),
                id,
                application.getString(R.string.by_folder),
                appName
            ).also { container ->
                addContainer(container)
                container.addToRegistry()

                val column = AudioDirectoryContainer.AUDIO_DATA_PATH
                val externalContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

                generateContainerStructure(
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
            application.getString(R.string.videos),
            appName
        ).apply {
            rootContainer.addContainer(this)

            AllVideoContainer(
                ALL_VIDEO.toString(),
                VIDEO_ID.toString(),
                application.getString(R.string.all),
                appName,
                baseUrl,
                contentResolver = application.contentResolver
            ).also { container ->
                addContainer(container)
                container.addToRegistry()
            }

            Container(
                VIDEO_BY_FOLDER.toString(),
                id,
                application.getString(R.string.by_folder),
                appName
            ).also { container ->
                addContainer(container)
                container.addToRegistry()

                val column = VideoDirectoryContainer.VIDEO_DATA_PATH
                val externalContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                generateContainerStructure(
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
        application
            .contentResolver
            .persistedUriPermissions
            .map { it.uri }
            .mapNotNull { DocumentFile.fromTreeUri(application, it) }
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
            database.directoryCacheQueries.selectByUri(uri.toString()).executeAsList().lastOrNull() ?: return false
        return containerRegistry[cachedEntry._id] != null
    }

    private fun queryOrCreateContainer(uri: Uri, parentContainer: Container): Container? {
        return (database.directoryCacheQueries.selectByUri(uri.toString()).executeAsList().lastOrNull()
            ?.let { cachedValue ->
                createContainer(cachedValue._id, parentContainer.rawId, cachedValue.name)
            } ?: createFolderContainer(uri, parentContainer.rawId))?.also {
            parentContainer.addContainer(it)
            it.addToRegistry()
        }
    }

    private fun createFolderContainer(uri: Uri, parentId: String): Container? = application
        .contentResolver
        .query(uri,
            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)

            if (cursor.moveToFirst()) {
                val id = randomId
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
    ) = Container(id.toString(), parentId, "${USER_DEFINED_PREFIX}$name", null)

    private fun queryUri(
        uri: Uri,
        parentContainer: Container,
    ) {
        getCachedFile(uri)?.apply {
            val id = "${TREE_PREFIX}$_id"
            when {
                mime.startsWith("image") -> {
                    parentContainer.addImageItem(
                        baseUrl = baseUrl,
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
                        baseUrl = baseUrl,
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
                        baseUrl = baseUrl,
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
        } ?: application
            .contentResolver
            .query(uri,
                arrayOf(
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.MIME_TYPE
                ), null, null, null)
            ?.use { cursor ->
                val mimeTypeColumn = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = randomId
                    val mimeType = cursor.getString(mimeTypeColumn)
                    when {
                        mimeType.startsWith("image") -> application.contentResolver.queryImages(uri) { _, title, _, size, width, height ->
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
                                baseUrl = baseUrl,
                                id = "${TREE_PREFIX}$id",
                                name = title,
                                mime = mimeType,
                                width = width,
                                height = height,
                                size = size)

                        }

                        mimeType.startsWith("video") -> application.contentResolver.queryVideos(uri) { _, title, creator, _, size, duration, width, height ->
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
                                baseUrl = baseUrl,
                                id = "${TREE_PREFIX}$id",
                                name = title,
                                mime = mimeType,
                                width = width,
                                height = height,
                                size = size,
                                duration = duration
                            )
                        }

                        mimeType.startsWith("audio") -> application.contentResolver.queryAudio(uri) { _, title, creator, _, size, duration, width, height, artist, album ->
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
                                baseUrl = baseUrl,
                                id = "${TREE_PREFIX}$id",
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
        return database.fileCacheQueries.selectByUri(uri.toString()).executeAsList().lastOrNull()
    }

    private fun generateContainerStructure(
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
        val folders: MutableMap<String, Map<String, Any>> = mutableMapOf()
        buildSet<String> {
            application.contentResolver.query(
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
                val childContainer = childContainerBuilder(
                    randomId.toString(),
                    rootContainer.rawId,
                    kv.key,
                    appName,
                    baseUrl,
                    ContentDirectory(kv.key),
                    application.contentResolver
                ).apply { addToRegistry() }

                rootContainer.addContainer(childContainer)

                populateFromMap(childContainer, kv.value as Map<String, Map<String, Any>>)
            }
        }

        populateFromMap(rootContainer, folders)
    }

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
    }
}
