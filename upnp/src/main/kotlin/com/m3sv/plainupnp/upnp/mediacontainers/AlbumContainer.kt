package com.m3sv.plainupnp.upnp.mediacontainers

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import org.fourthline.cling.support.model.container.Container
import timber.log.Timber

class AlbumContainer(
    id: String,
    parentID: String,
    title: String,
    creator: String,
    private val baseUrl: String,
    private val contentResolver: ContentResolver,
    private val artistId: String?
) : BaseContainer(id, parentID, title, creator) {

    private val artist: String? = null

    private val uri: Uri = artistId?.let {
        MediaStore.Audio.Artists.Albums.getContentUri("external", it.toLong())
    } ?: MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

    override fun getChildCount(): Int? {
        val columns: Array<String> = if (artistId == null)
            arrayOf(MediaStore.Audio.Albums._ID)
        else
            arrayOf(MediaStore.Audio.Artists.Albums.ALBUM)

        contentResolver
            .query(
                uri,
                columns,
                null,
                null,
                null
            ).use { cursor ->
                return cursor?.count ?: 0
            }
    }

    override fun getContainers(): List<Container> {
        Timber.d("Get albums!")

        val containers = mutableListOf<Container>()

        val columns: Array<String> = if (artistId == null)
            arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM)
        else
            arrayOf(MediaStore.Audio.Artists.Albums.ALBUM)

        contentResolver.query(
            uri,
            columns,
            null,
            null,
            null
        )?.use { cursor ->
            var albumIdColumn: Int? = null
            var albumColumn: Int? = null
            var artistsColumn: Int? = null

            if (artistId == null) {
                albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
                albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            } else {
                artistsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.Albums.ALBUM)
            }

            while (cursor.moveToNext()) {
                var albumId: String?
                val album: String?

                if (artistId == null) {
                    albumId = cursor.getInt(albumIdColumn!!).toString()
                    album = cursor.getString(albumColumn!!)
                } else {
                    album = cursor.getString(artistsColumn!!)
                    albumId = resolveAlbumId(album)
                }

                if (albumId != null && album != null) {
                    Timber.d(" current $id albumId : $albumId album : $album")
                    containers.add(
                        AllAudioContainer(
                            id = albumId,
                            parentID = id,
                            title = album,
                            creator = artist,
                            baseUrl = baseUrl,
                            contentResolver = contentResolver,
                            artist = null,
                            albumId = albumId
                        )
                    )
                } else {
                    Timber.e("Unable to get albumId or album")
                }
            }
        }

        return containers
    }

    private fun resolveAlbumId(album: String): String? {
        var result = ""
        val columns = arrayOf(MediaStore.Audio.Albums._ID)
        val where = MediaStore.Audio.Albums.ALBUM + "=?"
        val whereVal = arrayOf(album)

        contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            columns,
            where,
            whereVal, null
        )?.use { cursor ->
            if (cursor.moveToFirst())
                result =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums._ID)).toString()
        }

        return result
    }
}
