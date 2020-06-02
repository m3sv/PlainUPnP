package com.m3sv.plainupnp.upnp.mediacontainers

import android.content.ContentResolver
import android.provider.MediaStore
import com.m3sv.plainupnp.upnp.ContentDirectoryService
import org.fourthline.cling.support.model.PersonWithRole
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.MusicTrack
import org.seamless.util.MimeType

class AllAudioContainer(
    id: String,
    parentID: String,
    title: String,
    creator: String?,
    artist: String?,
    albumId: String?,
    private val baseUrl: String,
    private val contentResolver: ContentResolver
) : BaseContainer(id, parentID, title, creator) {

    private val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    private var orderBy: String? = null
    private var where: String? = null
    private var whereVal: Array<String>? = null

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

        contentResolver.query(
            uri,
            columns,
            where,
            whereVal,
            orderBy
        ).use { cursor ->
            return cursor?.count ?: 0
        }
    }

    override fun getContainers(): List<Container> {
        val columns = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM
        )

        contentResolver.query(
            uri,
            columns,
            where,
            whereVal,
            orderBy
        )?.use { cursor ->
            val audioIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val audioTitleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val audioArtistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val audioMimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val audioSizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val audioDurationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val audioAlbumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

            while (cursor.moveToNext()) {
                val id = ContentDirectoryService.AUDIO_PREFIX + cursor.getInt(audioIdColumn)
                val title = cursor.getString(audioTitleColumn)
                val creator = cursor.getString(audioArtistColumn)
                val type = cursor.getString(audioMimeTypeColumn)
                val size = cursor.getLong(audioSizeColumn)
                val duration = cursor.getLong(audioDurationColumn)
                val album = cursor.getString(audioAlbumColumn)

                val mimeType = type.substring(0, type.indexOf('/'))
                val mimeSubType = type.substring(type.indexOf('/') + 1)
                val res = Res(
                    MimeType(mimeType, mimeSubType),
                    size,
                    "http://$baseUrl/$id.$mimeSubType"
                )

                res.duration =
                    "${(duration / (1000 * 60 * 60))}:${duration % (1000 * 60 * 60) / (1000 * 60)}:${duration % (1000 * 60) / 1000}"

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
            }
        }

        return containers
    }
}
