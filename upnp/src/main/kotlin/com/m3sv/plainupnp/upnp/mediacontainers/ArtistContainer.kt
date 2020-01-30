/**
 * Copyright (C) 2013 Aur?lien Chabot <aurelien></aurelien>@chabot.fr>
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

import android.content.Context
import android.provider.MediaStore
import com.m3sv.plainupnp.common.ContentCache
import org.fourthline.cling.support.model.container.Container
import timber.log.Timber

class ArtistContainer(
        id: String,
        parentID: String,
        title: String,
        creator: String,
        baseURL: String,
        ctx: Context,
        private val cache: ContentCache
) : DynamicContainer(
        id,
        parentID,
        title,
        creator,
        baseURL,
        ctx,
        MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
) {

    init {
        Timber.d("Create ArtistContainer of id " + id + " , " + this.id)
    }

    override fun getChildCount(): Int? {
        val columns = arrayOf(MediaStore.Audio.Artists._ID)

        ctx.contentResolver.query(uri, columns, where, whereVal, orderBy).use { cursor ->
            return cursor?.count ?: 0
        }
    }

    override fun getContainers(): List<Container> {
        Timber.d("Get artist!")

        val columns = arrayOf(MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.ARTIST)
        ctx.contentResolver.query(uri, columns, null, null, null)?.apply {
            if (moveToFirst()) {
                do {
                    val artistId =
                            getInt(getColumnIndex(MediaStore.Audio.Artists._ID)).toString()
                    val artist =
                            getString(getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))

                    Timber.d("Add album container: artistId:$artistId; artistArtist:$artist")
                    containers.add(
                            AlbumContainer(
                                    artistId,
                                    id,
                                    artist,
                                    artist,
                                    baseURL,
                                    ctx,
                                    artistId,
                                    cache = cache
                            )
                    )

                } while (moveToNext())
            }

            close()
        }

        return containers
    }
}
