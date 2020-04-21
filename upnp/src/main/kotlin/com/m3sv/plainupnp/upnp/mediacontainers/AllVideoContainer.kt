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

package com.m3sv.plainupnp.upnp.mediacontainers

import android.content.ContentResolver
import android.provider.MediaStore
import com.m3sv.plainupnp.upnp.ContentDirectoryService
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.VideoItem
import org.seamless.util.MimeType

class AllVideoContainer(
    id: String,
    parentID: String,
    title: String,
    creator: String,
    private val baseUrl: String,
    private val contentResolver: ContentResolver
) : BaseContainer(
    id,
    parentID,
    title,
    creator
) {
    private val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    override fun getChildCount(): Int =
        contentResolver.query(
            uri,
            arrayOf(MediaStore.Video.Media._ID),
            null,
            null,
            null
        ).use { cursor ->
            return cursor?.count ?: 0
        }

    override fun getContainers(): List<Container> {
        val columns = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.WIDTH
        )

        contentResolver.query(
            uri,
            columns,
            null,
            null,
            null
        )?.use { cursor ->
            val videoIdColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)
            val videoTitleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val videoArtistColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
            val videoMimeTypeColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val videoSizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val videoDurationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val videoHeightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val videoWidthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)

            while (cursor.moveToNext()) {
                val id = ContentDirectoryService.VIDEO_PREFIX + cursor.getInt(videoIdColumn)
                val title = cursor.getString(videoTitleColumn)
                val creator = cursor.getString(videoArtistColumn)
                val mimeType = cursor.getString(videoMimeTypeColumn)
                val size = cursor.getLong(videoSizeColumn)
                val videoDuration = cursor.getLong(videoDurationColumn)
                val videoHeight = cursor.getLong(videoHeightColumn)
                val videoWidth = cursor.getLong(videoWidthColumn)

                val mimeTypeType = mimeType.substring(0, mimeType.indexOf('/'))
                val mimeTypeSubType = mimeType.substring(mimeType.indexOf('/') + 1)

                val res = Res(
                    MimeType(
                        mimeTypeType,
                        mimeTypeSubType
                    ),
                    size,
                    "http://$baseUrl/$id.$mimeTypeSubType"
                ).apply {
                    duration =
                        "${videoDuration / (1000 * 60 * 60)}:${videoDuration % (1000 * 60 * 60) / (1000 * 60)}:${videoDuration % (1000 * 60) / 1000}"
                    setResolution(videoWidth.toInt(), videoHeight.toInt())
                }

                addItem(VideoItem(id, parentID, title, creator, res))
            }
        }

        return containers
    }
}
