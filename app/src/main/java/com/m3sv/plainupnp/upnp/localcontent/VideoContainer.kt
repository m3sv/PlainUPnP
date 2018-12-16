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

package com.m3sv.plainupnp.upnp.localcontent

import android.content.Context
import android.provider.MediaStore
import org.droidupnp.legacy.mediaserver.ContentDirectoryService
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.VideoItem
import org.seamless.util.MimeType
import timber.log.Timber

class VideoContainer(
    id: String,
    parentID: String,
    title: String,
    creator: String,
    baseURL: String,
    ctx: Context
) : DynamicContainer(
    id,
    parentID,
    title,
    creator,
    baseURL,
    ctx,
    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
) {

    override fun getChildCount(): Int? {
        val columns = arrayOf(MediaStore.Video.Media._ID)
        val cursor = ctx.contentResolver.query(uri, columns, where, whereVal, orderBy) ?: return 0
        val result = cursor.count
        cursor.close()
        return result
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
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.WIDTH
        )

        ctx.contentResolver.query(uri, columns, where, whereVal, orderBy)?.apply {
            if (moveToFirst()) {
                do {
                    val id = ContentDirectoryService.VIDEO_PREFIX + getInt(
                        getColumnIndex(MediaStore.Video.Media._ID)
                    )
                    val title =
                        getString(getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                    val creator =
                        getString(getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST))
                    val filePath =
                        getString(getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    val mimeType =
                        getString(getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE))
                    val size =
                        getLong(getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                    val duration =
                        getLong(getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                    val height =
                        getLong(getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT))
                    val width =
                        getLong(getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH))

                    var extension = ""
                    val dot = filePath.lastIndexOf('.')
                    if (dot >= 0)
                        extension = filePath.substring(dot).toLowerCase()

                    val res = Res(
                        MimeType(
                            mimeType.substring(0, mimeType.indexOf('/')),
                            mimeType.substring(mimeType.indexOf('/') + 1)
                        ), size, "http://$baseURL/$id$extension"
                    )
                    res.duration = ((duration / (1000 * 60 * 60)).toString() + ":"
                            + duration % (1000 * 60 * 60) / (1000 * 60) + ":"
                            + duration % (1000 * 60) / 1000)
                    res.setResolution(width.toInt(), height.toInt())

                    addItem(VideoItem(id, parentID, title, creator, res))

                    Timber.v("Added video item $title from $filePath")
                } while (moveToNext())
            }
        }?.close()

        return containers
    }
}
