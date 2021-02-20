package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.upnp.ContentRepository.Companion.ALL_ALBUMS
import com.m3sv.plainupnp.upnp.ContentRepository.Companion.ALL_ARTISTS
import com.m3sv.plainupnp.upnp.ContentRepository.Companion.AUDIO_ID
import com.m3sv.plainupnp.upnp.ContentRepository.Companion.IMAGE_ID
import com.m3sv.plainupnp.upnp.ContentRepository.Companion.ROOT_ID
import com.m3sv.plainupnp.upnp.ContentRepository.Companion.SEPARATOR
import com.m3sv.plainupnp.upnp.ContentRepository.Companion.VIDEO_ID
import com.m3sv.plainupnp.upnp.mediacontainers.BaseContainer
import kotlinx.coroutines.runBlocking
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

    lateinit var contentRepository: ContentRepository

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
                            && root !in contentRepository.containerRegistry.keys
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

            val container: BaseContainer = if (subtype.isEmpty()) {
                contentRepository.containerRegistry[root] ?: throw noSuchObject
            } else {
                when (root) {
                    VIDEO_ID -> contentRepository.containerRegistry[subtype[0]] ?: throw noSuchObject
                    AUDIO_ID -> when {
                        subtype.size == 1 -> contentRepository.containerRegistry[subtype[0]] ?: throw noSuchObject
                        subtype.size == 2 && subtype[0] == ALL_ARTISTS -> {
                            val artistId = subtype[1].toString()
                            val parentId = "$AUDIO_ID$SEPARATOR${subtype[0]}"
                            Timber.d("Listing album of artist $artistId")


                            contentRepository.getAlbumContainerForArtist(artistId, parentId)
                        }
                        subtype.size == 2 && subtype[0] == ALL_ALBUMS -> {
                            val albumId = subtype[1].toString()
                            val parentId = "$AUDIO_ID$SEPARATOR${subtype[0]}"
                            Timber.d("Listing song of album $albumId")

                            contentRepository.getAudioContainerForAlbum(albumId, parentId)
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

                            contentRepository.getAudioContainerForAlbum(albumId, parentId)
                        }
                        else -> throw noSuchObject
                    }

                    IMAGE_ID -> contentRepository.containerRegistry[subtype[0]] ?: throw noSuchObject
                    else -> contentRepository.containerRegistry[subtype[0]] ?: throw noSuchObject
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

    companion object {
        private val noSuchObject =
            ContentDirectoryException(ContentDirectoryErrorCode.NO_SUCH_OBJECT)

        fun isRoot(parentId: String?) =
            parentId?.compareTo(ROOT_ID.toString()) == 0
    }
}
