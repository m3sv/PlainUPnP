package com.m3sv.plainupnp.upnp.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.m3sv.plainupnp.upnp.didl.ClingAudioItem
import com.m3sv.plainupnp.upnp.didl.ClingImageItem
import com.m3sv.plainupnp.upnp.didl.ClingVideoItem
import com.m3sv.plainupnp.upnp.manager.RenderItem
import timber.log.Timber
import javax.inject.Inject

class LaunchLocallyUseCase @Inject constructor(private val context: Context) {

    suspend operator fun invoke(item: RenderItem) {
        val uri = item.didlItem.uri
        if (uri != null) {
            val contentType = when (item.didlItem) {
                is ClingAudioItem -> "audio/*"
                is ClingImageItem -> "image/*"
                is ClingVideoItem -> "video/*"
                else -> null
            }

            if (contentType != null) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                        setDataAndType(Uri.parse(uri), contentType)
                        flags += Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

}
