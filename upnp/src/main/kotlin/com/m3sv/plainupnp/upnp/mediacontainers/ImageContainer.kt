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
import android.net.Uri
import android.provider.MediaStore
import com.m3sv.plainupnp.upnp.ContentDirectoryService
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.ImageItem
import org.seamless.util.MimeType
import timber.log.Timber

class ImageContainer(
    id: String,
    parentID: String?,
    title: String?,
    creator: String?,
    baseURL: String,
    private val contentResolver: ContentResolver
) : DynamicContainer(id, parentID, title, creator, baseURL) {

    private val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    override fun getChildCount(): Int? {
        contentResolver.query(uri, CHILD_COUNT_COLUMNS, null, null, null).use { cursor ->
            return cursor?.count ?: 0
        }
    }

    override fun getContainers(): List<Container> {
        val queryStart = System.currentTimeMillis()
        var processingStart = System.currentTimeMillis()
        contentResolver
            .query(
                uri,
                CONTAINERS_COLUMNS,
                null,
                null,
                null
            )?.use { cursor ->
                val end = System.currentTimeMillis()
                processingStart = System.currentTimeMillis()

                Timber.d("query took: ${end - queryStart} ms")

                with(cursor) {
                    if (moveToFirst()) {
                        do {
                            val id = ContentDirectoryService.IMAGE_PREFIX + getInt(
                                getColumnIndex(MediaStore.Images.Media._ID)
                            )
                            val title =
                                getString(getColumnIndex(MediaStore.Images.Media.TITLE))
                            val mime =
                                getString(getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
                            val size =
                                getLong(getColumnIndex(MediaStore.Images.Media.SIZE))
                            val height =
                                getLong(getColumnIndex(MediaStore.Images.Media.HEIGHT))
                            val width =
                                getLong(getColumnIndex(MediaStore.Images.Media.WIDTH))

                            val mimeTypeSeparatorPosition = mime.indexOf('/')
                            val mimeType = mime.substring(0, mimeTypeSeparatorPosition)
                            val mimeSubType = mime.substring(mimeTypeSeparatorPosition + 1)

                            val res = Res(
                                MimeType(mimeType, mimeSubType),
                                size,
                                "http://$baseURL/$id.$mimeSubType"
                            ).apply {
                                setResolution(width.toInt(), height.toInt())
                            }

                            addItem(
                                ImageItem(
                                    id,
                                    parentID,
                                    title,
                                    "",
                                    res
                                )
                            )
                        } while (moveToNext())
                    }
                }
            }

        val end = System.currentTimeMillis()

        Timber.d("Processing query took: ${end - processingStart} ms")
        return containers
    }

    companion object {
        private val CHILD_COUNT_COLUMNS = arrayOf(MediaStore.Images.Media._ID)

        private val CONTAINERS_COLUMNS = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.WIDTH
        )
    }
}
