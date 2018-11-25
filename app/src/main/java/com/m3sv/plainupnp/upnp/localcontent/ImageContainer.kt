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
import org.fourthline.cling.support.model.item.ImageItem
import org.seamless.util.MimeType
import timber.log.Timber

class ImageContainer(
    id: String,
    parentID: String?,
    title: String?,
    creator: String?,
    baseURL: String?,
    context: Context
) : DynamicContainer(id, parentID, title, creator, baseURL!!, context, null) {

    init {
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    override fun getChildCount(): Int? {
        val columns = arrayOf(MediaStore.Images.Media._ID)
        val cursor = ctx.contentResolver.query(uri, columns, where, whereVal, orderBy) ?: return 0
        val result = cursor.count
        cursor.close()
        return result
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

        ctx.contentResolver.query(uri, columns, where, whereVal, orderBy)?.let {
            if (it.moveToFirst()) {
                do {
                    val id = ContentDirectoryService.IMAGE_PREFIX + it.getInt(
                        it.getColumnIndex(MediaStore.Images.Media._ID)
                    )
                    val title =
                        it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE))
                    val filePath =
                        it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                    val mimeType =
                        it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))
                    val size =
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                    val height =
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT))
                    val width =
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH))

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
                    res.setResolution(width.toInt(), height.toInt())

                    addItem(ImageItem(id, parentID, title, "", res))

                    Timber.v("Added image item $title from $filePath")
                } while (it.moveToNext())
            }
            it.close()
        }

        return containers
    }
}
