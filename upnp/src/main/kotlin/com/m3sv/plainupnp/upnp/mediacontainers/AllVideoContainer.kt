package com.m3sv.plainupnp.upnp.mediacontainers

import android.content.ContentResolver
import android.provider.MediaStore
import com.m3sv.plainupnp.upnp.util.addVideoItem
import com.m3sv.plainupnp.upnp.util.queryVideos
import org.fourthline.cling.support.model.container.Container

class AllVideoContainer(
    id: String,
    parentID: String,
    title: String,
    creator: String,
    private val baseUrl: String,
    private val contentResolver: ContentResolver,
) : BaseContainer(
    id,
    parentID,
    title,
    creator
) {
    private val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    override fun getChildCount(): Int =
        contentResolver.query(
            uri,
            arrayOf(MediaStore.Video.Media._ID),
            null,
            null,
            null
        )?.use { it.count } ?: 0

    override fun getContainers(): List<Container> {
        contentResolver.queryVideos { id, title, creator, mimeType, size, duration, width, height ->
            addVideoItem(baseUrl, id, title, mimeType, width, height, size, duration)
        }

        return containers
    }
}
