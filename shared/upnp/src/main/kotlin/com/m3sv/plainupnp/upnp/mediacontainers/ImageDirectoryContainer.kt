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
import com.m3sv.plainupnp.common.util.isQ
import com.m3sv.plainupnp.upnp.ContentDirectoryService
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.ImageItem
import org.seamless.util.MimeType

class ImageDirectoryContainer(
    id: String,
    parentID: String?,
    title: String?,
    creator: String?,
    private val baseUrl: String,
    private val directory: ContentDirectory,
    private val contentResolver: ContentResolver
) : BaseContainer(id, parentID, title, creator) {

    private val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    private val selection: String = "$IMAGE_DATA_PATH LIKE ?"

    private val selectionArgs: Array<String> = arrayOf("%${directory.name}/${if (isQ) "" else "%"}")

    override fun getChildCount(): Int? {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            IMAGE_DATA_PATH
        )

        contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        ).use { cursor ->
            return cursor?.count ?: 0
        }
    }

    override fun getContainers(): List<Container> {
        if (items.isNotEmpty() || containers.isNotEmpty())
            return containers

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            IMAGE_DATA_PATH,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.WIDTH
        )

        contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val imagesIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val imagesTitleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE)
            val imagesMimeTypeColumn = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
            val imagesMediaSizeColumn = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
            val imagesHeightColumn = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
            val imagesWidthColumn = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
            val dataColumn = cursor.getColumnIndex(IMAGE_DATA_PATH)

            while (cursor.moveToNext()) {
                if (!isQ && !directory.samePath(cursor.getString(dataColumn)))
                    continue

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
        val IMAGE_DATA_PATH = if (isQ)
            MediaStore.Images.Media.RELATIVE_PATH
        else
            MediaStore.Images.Media.DATA
    }
}
