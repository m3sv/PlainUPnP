package com.m3sv.plainupnp.upnp.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.m3sv.plainupnp.upnp.ContentDirectoryService

fun ContentResolver.queryImages(
    uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    block: (
        id: String,
        title: String,
        mimeType: String,
        size: Long,
        width: Long,
        height: Long,
    ) -> Unit,
) {
    query(
        uri,
        IMAGE_COLUMNS,
        null,
        null,
        null
    )?.use { cursor ->
        val imagesIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        val imagesTitleColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
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

            block(id, title, mime, size, width, height)
        }
    }
}

private val IMAGE_COLUMNS = arrayOf(
    MediaStore.Images.Media._ID,
    MediaStore.Images.Media.DISPLAY_NAME,
    MediaStore.Images.Media.MIME_TYPE,
    MediaStore.Images.Media.SIZE,
    MediaStore.Images.Media.HEIGHT,
    MediaStore.Images.Media.WIDTH
)

fun ContentResolver.queryVideos(
    uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
    block: (
        id: String,
        title: String,
        creator: String,
        mimeType: String,
        size: Long,
        duration: Long,
        width: Long,
        height: Long,
    ) -> Unit,
) {
    query(
        uri,
        VIDEO_COLUMNS,
        null,
        null,
        null
    )?.use { cursor ->
        val videoIdColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)
        val videoTitleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val videoArtistColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
        val videoMimeTypeColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
        val videoSizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val videoDurationColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val videoHeightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
        val videoWidthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)

        while (cursor.moveToNext()) {
            val id = ContentDirectoryService.VIDEO_PREFIX + cursor.getInt(videoIdColumn)
            val title = cursor.getString(videoTitleColumn)
            val creator = cursor.getString(videoArtistColumn)
            val mimeType = cursor.getString(videoMimeTypeColumn)
            val size = cursor.getLong(videoSizeColumn)
            val videoDuration = cursor.getLong(videoDurationColumn)
            val videoHeight = cursor.getLong(videoHeightColumn)
            val videoWidth = cursor.getLong(videoWidthColumn)

            block(id, title, creator, mimeType, size, videoDuration, videoWidth, videoHeight)
        }
    }
}

private val VIDEO_COLUMNS = arrayOf(
    MediaStore.Video.Media._ID,
    MediaStore.Video.Media.DISPLAY_NAME,
    MediaStore.Video.Media.ARTIST,
    MediaStore.Video.Media.MIME_TYPE,
    MediaStore.Video.Media.SIZE,
    MediaStore.Video.Media.DURATION,
    MediaStore.Video.Media.HEIGHT,
    MediaStore.Video.Media.WIDTH
)
