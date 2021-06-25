package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl.Companion.ALL_ALBUMS
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl.Companion.ALL_ARTISTS
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl.Companion.AUDIO_ID
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl.Companion.SEPARATOR
import com.m3sv.plainupnp.upnp.mediacontainers.BaseContainer
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException
import org.fourthline.cling.support.contentdirectory.DIDLParser
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.BrowseResult
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.SortCriterion
import timber.log.Timber

class ContentDirectoryService : AbstractContentDirectoryService() {

    lateinit var contentRepository: UpnpContentRepositoryImpl

    override fun browse(
        objectID: String,
        browseFlag: BrowseFlag,
        filter: String,
        firstResult: Long,
        maxResults: Long,
        orderby: Array<SortCriterion>,
    ): BrowseResult {
        contentRepository.init

        try {
            val url = objectID
                .split(SEPARATOR)
                .map(String::toLong)
                .takeIf { it.isNotEmpty() }
                ?: throw ContentDirectoryException(
                    ContentDirectoryErrorCode.CANNOT_PROCESS,
                    "Invalid type!"
                )

            val root = url.first()
            val end = url.last()

            Timber.d("Browsing type $objectID")

            val container: BaseContainer? = when {
                // drop AUDIO_ID, we don't care about it
                root == AUDIO_ID && url.size > 1 -> handleAudioContainerSelection(url.drop(1))
                    ?: contentRepository.containerRegistry[end]
                else -> contentRepository.containerRegistry[end]
            }

            return getBrowseResult(container ?: throw noSuchObject)
        } catch (ex: Exception) {
            Timber.e(ex)
            throw ContentDirectoryException(
                ContentDirectoryErrorCode.CANNOT_PROCESS,
                ex.toString()
            )
        }
    }

    private fun handleAudioContainerSelection(
        url: List<Long>,
    ): BaseContainer? {
        val root = url[0]
        val tail = url.last()

        return when (root) {
            ALL_ARTISTS -> {
                when (url.size) {
                    2 -> {
                        val artistId = tail.toString()
                        val parentId = "$AUDIO_ID$SEPARATOR${root}"
                        Timber.d("Listing album of artist $artistId")

                        contentRepository.getAlbumContainerForArtist(artistId, parentId)
                    }
                    3 -> {
                        val albumId = url[2].toString()
                        val parentId =
                            "$AUDIO_ID$SEPARATOR${root}$SEPARATOR${tail}"

                        Timber.d(
                            "Listing song of album %s for artist %s",
                            albumId,
                            url[2]
                        )

                        contentRepository.getAudioContainerForAlbum(albumId, parentId)
                    }
                    else -> null
                }

            }
            ALL_ALBUMS -> {
                if (url.size == 2) {
                    val albumId = tail.toString()
                    val parentId = "$AUDIO_ID$SEPARATOR${root}"
                    Timber.d("Listing song of album $albumId")

                    contentRepository.getAudioContainerForAlbum(albumId, parentId)
                } else null
            }

            else -> null
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
            Timber.e(ex)
            throw ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString())
        }

        return BrowseResult(answer, count, count)
    }

    companion object {
        private val noSuchObject
            get() = ContentDirectoryException(ContentDirectoryErrorCode.NO_SUCH_OBJECT)
    }
}
