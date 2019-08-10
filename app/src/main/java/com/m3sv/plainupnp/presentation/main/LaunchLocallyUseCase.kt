package com.m3sv.plainupnp.presentation.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.Consumable
import com.m3sv.plainupnp.upnp.LocalModel
import timber.log.Timber
import javax.inject.Inject

class LaunchLocallyUseCase @Inject constructor(private val context: Context) {
    fun execute(item: Consumable<LocalModel?>) {
        item.consume()?.let {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.uri)).apply {
                    setDataAndType(Uri.parse(it.uri), it.contentType)
                }

                val title = context.getString(R.string.play_with)
                val chooser = Intent.createChooser(intent, title).apply { flags += Intent.FLAG_ACTIVITY_NEW_TASK }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(chooser)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}