package com.m3sv.plainupnp.upnp.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.m3sv.plainupnp.ContentCache
import com.m3sv.plainupnp.upnp.LocalModel
import com.m3sv.plainupnp.upnp.RenderItem
import com.m3sv.plainupnp.upnp.didl.ClingAudioItem
import com.m3sv.plainupnp.upnp.didl.ClingImageItem
import com.m3sv.plainupnp.upnp.didl.ClingVideoItem
import timber.log.Timber
import javax.inject.Inject

class LaunchLocallyUseCase @Inject constructor(private val context: Context,
                                               private val contentCache: ContentCache) {
    fun execute(item: RenderItem) {
        item.item.uri?.let { uri ->
            val contentType = when (item.item) {
                is ClingAudioItem -> "audio/*"
                is ClingImageItem -> "image/*"
                is ClingVideoItem -> "video/*"
                else -> null
            }

            contentType?.let {
                LocalModel(
                        contentCache.get(item.item.id) ?: uri,
                        contentType
                )
            }?.let {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.uri)).apply {
                        setDataAndType(Uri.parse(it.uri), it.contentType)
                    }
                    // TODO Add title
//                    val title = context.getString(R.string.play_with)
                    val chooser = Intent.createChooser(intent, "").apply { flags += Intent.FLAG_ACTIVITY_NEW_TASK }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(chooser)
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }
}