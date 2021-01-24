package com.m3sv.plainupnp.upnp.mediacontainers

import android.content.ContentResolver
import android.provider.MediaStore
import com.m3sv.plainupnp.upnp.util.queryVideos
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.container.Container
import org.fourthline.cling.support.model.item.VideoItem
import org.seamless.util.MimeType

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
            val mimeTypeType = mimeType.substring(0, mimeType.indexOf('/'))
            val mimeTypeSubType = mimeType.substring(mimeType.indexOf('/') + 1)

            val res = Res(
                MimeType(
                    mimeTypeType,
                    mimeTypeSubType
                ),
                size,
                "http://$baseUrl/$id.$mimeTypeSubType"
            ).also { res ->
                res.duration =
                    "${duration / (1000 * 60 * 60)}:${duration % (1000 * 60 * 60) / (1000 * 60)}:${duration % (1000 * 60) / 1000}"
                res.setResolution(width.toInt(), height.toInt())
            }

            addItem(VideoItem(id, parentID, title, creator, res))
        }

        return containers
    }
}
