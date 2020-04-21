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

class AllImagesContainer(
    id: String,
    parentID: String?,
    title: String?,
    creator: String?,
    private val baseUrl: String,
    private val contentResolver: ContentResolver
) : BaseContainer(id, parentID, title, creator) {

    private val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    override fun getChildCount(): Int? = contentResolver.query(
        uri,
        CHILD_COUNT_COLUMNS,
        null,
        null,
        null
    ).use { cursor ->
        return cursor?.count ?: 0
    }

    override fun getContainers(): List<Container> {
        contentResolver
            .query(
                uri,
                CONTAINERS_COLUMNS,
                null,
                null,
                null
            )?.use { cursor ->
                val imagesIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val imagesTitleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE)
                val imagesMimeTypeColumn = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                val imagesMediaSizeColumn = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                val imagesHeightColumn = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                val imagesWidthColumn = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)

                while (cursor.moveToNext()) {
                    val id = ContentDirectoryService.IMAGE_PREFIX + cursor.getInt(imagesIdColumn)
                    val title = cursor.getString(imagesTitleColumn)
                    val mime = cursor.getString(imagesMimeTypeColumn)
                    val size = cursor.getLong(imagesMediaSizeColumn)
                    val height = cursor.getLong(imagesHeightColumn)
                    val width = cursor.getLong(imagesWidthColumn)

                    val mimeTypeSeparatorPosition = mime.indexOf('/')
                    val mimeType = mime.substring(0, mimeTypeSeparatorPosition)
                    val mimeSubType = mime.substring(mimeTypeSeparatorPosition + 1)

                    val res = Res(
                        MimeType(mimeType, mimeSubType),
                        size,
                        "http://$baseUrl/$id.$mimeSubType"
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
                }
            }

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
