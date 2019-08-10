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
import android.net.Uri
import android.provider.MediaStore
import com.m3sv.plainupnp.ContentCache
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
        context: Context,
        uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        private val cache: ContentCache
) : DynamicContainer(id, parentID, title, creator, baseURL, context, uri) {

    override fun getChildCount(): Int? {
        val columns = arrayOf(MediaStore.Images.Media._ID)
        ctx.contentResolver.query(uri, columns, where, whereVal, orderBy).use { cursor ->
            return cursor?.count ?: 0
        }
    }

    override fun getContainers(): List<Container> {
        val columns = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH
        )

        ctx.contentResolver.query(uri, columns, where, whereVal, orderBy)?.use { cursor ->
            with(cursor) {
                if (moveToFirst()) {
                    do {
                        val id =
                                ContentDirectoryService.IMAGE_PREFIX + getInt(getColumnIndex(MediaStore.Images.Media._ID))
                        val title =
                                getString(getColumnIndexOrThrow(MediaStore.Images.Media.TITLE))
                        val filePath =
                                getString(getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                        val mimeType =
                                getString(getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))
                        val size =
                                getLong(getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                        val height =
                                getLong(getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT))
                        val width =
                                getLong(getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH))

                        cache.put(id, filePath)
                        var extension = ""
                        val dot = filePath.lastIndexOf('.')
                        if (dot >= 0)
                            extension = filePath.substring(dot).toLowerCase()

                        val res = Res(
                                MimeType(
                                        mimeType.substring(0, mimeType.indexOf('/')),
                                        mimeType.substring(mimeType.indexOf('/') + 1)
                                ),
                                size,
                                "http://$baseURL/$id$extension"
                        )
                        res.setResolution(width.toInt(), height.toInt())

                        addItem(ImageItem(id,
                                parentID,
                                title,
                                "",
                                res))
                        Timber.v("Added image item $title from $filePath")
                    } while (moveToNext())
                }
            }
        }

        return containers
    }
}
