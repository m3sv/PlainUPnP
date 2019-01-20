package com.m3sv.plainupnp.upnp.localcontent

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import org.fourthline.cling.support.model.container.Container
import timber.log.Timber

class AlbumContainer(
    id: String,
    parentID: String,
    title: String,
    creator: String,
    baseURL: String,
    ctx: Context,
    private val artistId: String?,
    uri: Uri = artistId?.let {
        MediaStore.Audio.Artists.Albums.getContentUri("external", it.toLong())
    } ?: MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
) : DynamicContainer(
    id,
    parentID,
    title,
    creator,
    baseURL,
    ctx,
    uri
) {
    private val artist: String? = null

    override fun getChildCount(): Int? {
        val columns: Array<String> = if (artistId == null)
            arrayOf(MediaStore.Audio.Albums._ID)
        else
            arrayOf(MediaStore.Audio.Artists.Albums.ALBUM)

        val cursor = ctx.contentResolver.query(uri, columns, where, whereVal, orderBy) ?: return 0
        val count = cursor.count
        cursor.close()
        return count
    }

    override fun getContainers(): List<Container> {
        Timber.d("Get albums!")

        val columns: Array<String> = if (artistId == null)
            arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM)
        else
            arrayOf(MediaStore.Audio.Artists.Albums.ALBUM)

        ctx.contentResolver.query(uri, columns, where, whereVal, orderBy)?.apply {
            if (moveToFirst()) {
                do {
                    var albumId: String? = null
                    val album: String?
                    if (artistId == null) {
                        albumId = getInt(getColumnIndex(MediaStore.Audio.Albums._ID)).toString()
                        album = getString(getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM))
                    } else {
                        album =
                                getString(getColumnIndexOrThrow(MediaStore.Audio.Artists.Albums.ALBUM))

                        albumId = resolveAlbumId(album, albumId)
                    }

                    if (albumId != null && album != null) {
                        Timber.d(" current $id albumId : $albumId album : $album")
                        containers.add(
                            AudioContainer(
                                albumId,
                                id,
                                album,
                                artist,
                                baseURL,
                                ctx,
                                null,
                                albumId
                            )
                        )
                    } else {
                        Timber.d("Unable to get albumId or album")
                    }
                } while (moveToNext())
            }
        }?.close()

        return containers
    }

    private fun resolveAlbumId(album: String, albumId: String?): String? {
        var result = albumId
        val columns2 = arrayOf(MediaStore.Audio.Albums._ID)
        val where2 = MediaStore.Audio.Albums.ALBUM + "=?"
        val whereVal2 = arrayOf(album)

        ctx.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            columns2, where2, whereVal2, null
        )?.apply {
            if (moveToFirst())
                result = getInt(getColumnIndex(MediaStore.Audio.Albums._ID)).toString()
        }?.close()
        return result
    }
}
