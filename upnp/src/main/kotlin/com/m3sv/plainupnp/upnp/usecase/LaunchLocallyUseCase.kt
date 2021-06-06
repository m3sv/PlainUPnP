package com.m3sv.plainupnp.upnp.usecase

import android.app.Application
import android.content.Intent
import android.net.Uri
import com.m3sv.plainupnp.upnp.didl.ClingMedia
import com.m3sv.plainupnp.upnp.manager.RenderItem
import timber.log.Timber
import javax.inject.Inject

class LaunchLocallyUseCase @Inject constructor(private val application: Application) {

    operator fun invoke(item: RenderItem) {
        val uri = item.didlItem.uri
        if (uri != null) {
            val contentType = when (item.didlItem) {
                is ClingMedia.Audio -> "audio/*"
                is ClingMedia.Image -> "image/*"
                is ClingMedia.Video -> "video/*"
                else -> null
            }

            if (contentType != null) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                        setDataAndType(Uri.parse(uri), contentType)
                        flags += Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(application.packageManager) != null) {
                        application.startActivity(intent)
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

}
