/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien></aurelien>@chabot.fr>
 *
 *
 * This file is part of DroidUPNP.
 *
 *
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */

package org.droidupnp.legacy.mediaserver

import android.content.Context
import android.preference.PreferenceManager
import android.text.TextUtils
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.CONTENT_DIRECTORY_AUDIO
import com.m3sv.plainupnp.common.CONTENT_DIRECTORY_IMAGE
import com.m3sv.plainupnp.common.CONTENT_DIRECTORY_VIDEO
import com.m3sv.plainupnp.upnp.localcontent.*
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

class ContentDirectoryService : AbstractContentDirectoryService {

    private lateinit var ctx: Context

    constructor() : super()

    constructor(ctx: Context, baseURL: String) {
        this.ctx = ctx
        this.baseURL = baseURL
    }

    fun setContext(ctx: Context) {
        this.ctx = ctx
    }

    fun setBaseURL(baseURL: String) {
        this.baseURL = baseURL
    }

    private var baseURL: String? = null

    @Throws(ContentDirectoryException::class)
    override fun browse(
        objectID: String, browseFlag: BrowseFlag,
        filter: String, firstResult: Long, maxResults: Long,
        orderby: Array<SortCriterion>
    ): BrowseResult {
        Timber.d("Will browse $objectID")

        try {
            val didl = DIDLContent()
            val ss = TextUtils.SimpleStringSplitter(SEPARATOR)
            ss.setString(objectID)

            var type = -1
            val subtype = ArrayList<Int>()

            for (s in ss) {
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
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx)

            val appName = ctx.getString(R.string.app_name)
            val rootContainer = CustomContainer(
                "" + ROOT_ID, "" + ROOT_ID,
                appName, appName, baseURL
            )

            // Video
            var videoContainer: Container? = null
            var allVideoContainer: Container? = null

            if (sharedPref.getBoolean(CONTENT_DIRECTORY_VIDEO, true)) {
                videoContainer = CustomContainer(
                    "" + VIDEO_ID, "" + ROOT_ID,
                    VIDEO_TXT, appName, baseURL
                )

                with(rootContainer) {
                    addContainer(videoContainer)
                    childCount = rootContainer.childCount + 1
                }

                allVideoContainer = VideoContainer(
                    "" + ALL_ID, "" + VIDEO_ID,
                    "All", appName, baseURL, ctx
                )

                with(videoContainer) {
                    addContainer(allVideoContainer)
                    childCount = videoContainer.childCount + 1
                }
            }

            // Audio
            var audioContainer: Container? = null
            var artistAudioContainer: Container? = null
            var albumAudioContainer: Container? = null
            var allAudioContainer: Container? = null

            if (sharedPref.getBoolean(CONTENT_DIRECTORY_AUDIO, true)) {
                audioContainer = CustomContainer(
                    "" + AUDIO_ID, "" + ROOT_ID,
                    AUDIO_TXT, appName, baseURL
                )

                with(rootContainer) {
                    addContainer(audioContainer)
                    childCount = rootContainer.childCount + 1
                }

                with(audioContainer) {
                    artistAudioContainer = ArtistContainer(
                        "" + ARTIST_ID, "" + AUDIO_ID,
                        "Artist", appName, baseURL, ctx
                    )

                    addContainer(artistAudioContainer)
                    childCount = audioContainer.childCount + 1

                    albumAudioContainer = AlbumContainer(
                        "" + ALBUM_ID, "" + AUDIO_ID,
                        "Album", appName, baseURL, ctx, null
                    )

                    addContainer(albumAudioContainer)
                    childCount = audioContainer.childCount + 1

                    allAudioContainer = AudioContainer(
                        "" + ALL_ID, "" + AUDIO_ID,
                        "All", appName, baseURL, ctx, null, null
                    )

                    addContainer(allAudioContainer)
                    childCount = audioContainer.childCount + 1
                }
            }

            // Image
            var imageContainer: Container? = null
            var allImageContainer: Container? = null
            if (sharedPref.getBoolean(CONTENT_DIRECTORY_IMAGE, true)) {
                imageContainer = CustomContainer(
                    "" + IMAGE_ID, "" + ROOT_ID, IMAGE_TXT,
                    appName, baseURL
                )
                rootContainer.addContainer(imageContainer)
                rootContainer.childCount = rootContainer.childCount!! + 1

                allImageContainer = ImageContainer(
                    "" + ALL_ID, "" + IMAGE_ID, "All",
                    appName, baseURL!!, ctx
                )
                imageContainer.addContainer(allImageContainer)
                imageContainer.childCount = imageContainer.childCount!! + 1
            }

            if (subtype.size == 0) {
                if (type == ROOT_ID) container = rootContainer
                if (type == AUDIO_ID) container = audioContainer
                if (type == VIDEO_ID) container = videoContainer
                if (type == IMAGE_ID) container = imageContainer
            } else {
                if (type == VIDEO_ID) {
                    if (subtype[0] == ALL_ID) {
                        Timber.d("Listing all videos...")
                        container = allVideoContainer
                    }
                } else if (type == AUDIO_ID) {
                    if (subtype.size == 1) {
                        when {
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
                    } else if (subtype.size == 2 && subtype[0] == ARTIST_ID) {
                        val artistId = "" + subtype[1]
                        val parentId = "" + AUDIO_ID + SEPARATOR + subtype[0]
                        Timber.d("Listing album of artist $artistId")
                        container = AlbumContainer(
                            artistId, parentId, "",
                            appName, baseURL, ctx, artistId
                        )
                    } else if (subtype.size == 2 && subtype[0] == ALBUM_ID) {
                        val albumId = "" + subtype[1]
                        val parentId = "" + AUDIO_ID + SEPARATOR + subtype[0]
                        Timber.d("Listing song of album $albumId")
                        container = AudioContainer(
                            albumId, parentId, "",
                            appName, baseURL, ctx, null, albumId
                        )
                    } else if (subtype.size == 3 && subtype[0] == ARTIST_ID) {
                        val albumId = "" + subtype[2]
                        val parentId =
                            "" + AUDIO_ID + SEPARATOR + subtype[0] + SEPARATOR + subtype[1]
                        Timber.d("Listing song of album %s for artist %s", albumId, subtype[1])
                        container = AudioContainer(
                            albumId, parentId, "",
                            appName, baseURL, ctx, null, albumId
                        )
                    }
                } else if (type == IMAGE_ID) {
                    if (subtype[0] == ALL_ID) {
                        Timber.d("Listing all images...")
                        container = allImageContainer
                    }
                }
            }

            if (container != null) {
                Timber.d("List container...")

                // Get container first
                for (c in container.containers)
                    didl.addContainer(c)

                Timber.d("List item...")

                // Then get item
                for (i in container.items)
                    didl.addItem(i)

                Timber.d("Return result...")

                val count = container.childCount!!
                Timber.d("Child count: $count")
                var answer = ""
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
            throw ContentDirectoryException(
                ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString()
            )
        }

        Timber.e("No container for this ID !!!")
        throw ContentDirectoryException(ContentDirectoryErrorCode.NO_SUCH_OBJECT)
    }

    companion object {
        const val SEPARATOR = '$'

        // Type
        const val ROOT_ID = 0
        const val VIDEO_ID = 1
        const val AUDIO_ID = 2
        const val IMAGE_ID = 3

        // Test
        const val VIDEO_TXT = "Videos"
        const val AUDIO_TXT = "Music"
        const val IMAGE_TXT = "Images"

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
    }
}
