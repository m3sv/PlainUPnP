package com.m3sv.plainupnp.upnp.mediacontainers

import android.content.ContentResolver
import android.os.Build
import android.provider.MediaStore
import com.m3sv.plainupnp.common.util.isQ
import com.m3sv.plainupnp.upnp.ContentDirectoryService
import org.fourthline.cling.support.model.PersonWithRole
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.MusicTrack
import org.seamless.util.MimeType

class AudioDirectoryContainer(
    id: String,
    parentID: String,
    title: String,
    creator: String?,
    artist: String? = null,
    albumId: String? = null,
    private val baseUrl: String,
    private val directory: ContentDirectory,
    private val contentResolver: ContentResolver
) : BaseContainer(id, parentID, title, creator) {

    private var orderBy: String? = null

    private var selection: String = "$AUDIO_DATA_PATH LIKE ?"

    private var selectionArgs: Array<String> = arrayOf("%${directory.name}/${if (isQ) "" else "%"}")

    init {
        val directoryClause = " ,$selection"

        if (artist != null) {
            selection = "${MediaStore.Audio.Media.ARTIST} = ?${directoryClause}"
            selectionArgs = arrayOf(artist, "%$directory/")
            orderBy = MediaStore.Audio.Media.ALBUM
        }

        if (albumId != null) {
            selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?${directoryClause}"
            selectionArgs = arrayOf(albumId, "%$directory/")
            orderBy = MediaStore.Audio.Media.TRACK
        }
    }

    override fun getChildCount(): Int? {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            AUDIO_DATA_PATH
        )

        contentResolver.query(
            URI,
            projection,
            selection,
            selectionArgs,
            orderBy
        ).use { cursor ->
            return cursor?.count ?: 0
        }
    }

    override fun getContainers(): List<Container> {
        if (items.isNotEmpty() || containers.isNotEmpty())
            return containers

        val columns = arrayOf(
            MediaStore.Audio.Media._ID,
            AUDIO_DATA_PATH,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM
        )

        contentResolver.query(
            URI,
            columns,
            selection,
            selectionArgs,
            orderBy
        )?.use { cursor ->
            val audioIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val audioTitleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val audioArtistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val audioMimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val audioSizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val audioDurationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val audioAlbumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val dataColumn = cursor.getColumnIndex(AUDIO_DATA_PATH)

            while (cursor.moveToNext()) {
                if (!isQ && !directory.samePath(cursor.getString(dataColumn)))
                    continue

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

    companion object {
        private val URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val AUDIO_DATA_PATH = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Audio.Media.RELATIVE_PATH
        else
            MediaStore.Audio.Media.DATA
    }
}
