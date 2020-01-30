package com.m3sv.plainupnp.upnp.mediacontainers

import android.content.Context
import android.provider.MediaStore
import com.m3sv.plainupnp.common.ContentCache
import com.m3sv.plainupnp.upnp.ContentDirectoryService
import org.fourthline.cling.support.model.PersonWithRole
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.MusicTrack
import org.seamless.util.MimeType
import timber.log.Timber

class AudioContainer(
        id: String,
        parentID: String,
        title: String,
        creator: String?,
        baseURL: String,
        ctx: Context,
        artist: String?,
        albumId: String?,
        private val cache: ContentCache
) : DynamicContainer(
        id,
        parentID,
        title,
        creator,
        baseURL,
        ctx,
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
) {

    init {
        if (artist != null) {
            where = MediaStore.Audio.Media.ARTIST + "=?"
            whereVal = arrayOf(artist)
            orderBy = MediaStore.Audio.Media.ALBUM
        }

        if (albumId != null) {
            where = MediaStore.Audio.Media.ALBUM_ID + "=?"
            whereVal = arrayOf(albumId)
            orderBy = MediaStore.Audio.Media.TRACK
        }
    }

    override fun getChildCount(): Int? {
        val columns = arrayOf(MediaStore.Audio.Media._ID)

        ctx.contentResolver.query(uri, columns, where, whereVal, orderBy).use { cursor ->
            return cursor?.count ?: 0
        }
    }

    override fun getContainers(): List<Container> {
        val columns = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM
        )

        ctx.contentResolver.query(uri, columns, where, whereVal, orderBy)?.use { cursor ->
            with(cursor) {
                if (moveToFirst()) {
                    do {
                        val id = ContentDirectoryService.AUDIO_PREFIX + getInt(getColumnIndex(MediaStore.Audio.Media._ID))
                        val title = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                        val creator = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                        val filePath = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                        val mimeType = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE))
                        val size = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                        val duration = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                        val album = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))


                        var extension = ""

                        val dot = filePath.lastIndexOf('.')
                        if (dot >= 0)
                            extension = filePath.substring(dot).toLowerCase()

                        cache.put(id, filePath)

                        val res = Res(
                                MimeType(
                                        mimeType.substring(0, mimeType.indexOf('/')),
                                        mimeType.substring(mimeType.indexOf('/') + 1)
                                ),
                                size,
                                "http://$baseURL/$id$extension"
                        )

                        res.duration = ((duration / (1000 * 60 * 60)).toString() + ":"
                                + duration % (1000 * 60 * 60) / (1000 * 60) + ":"
                                + duration % (1000 * 60) / 1000)

                        addItem(
                                MusicTrack(
                                        id,
                                        parentID,
                                        title,
                                        creator,
                                        album,
                                        PersonWithRole(creator, "Performer"),
                                        res
                                )
                        )

                        Timber.v("Added audio item $title from $filePath")

                    } while (moveToNext())
                }
            }
        }
        return containers
    }
}
