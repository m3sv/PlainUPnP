package com.m3sv.plainupnp.upnp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class NotificationBuilder(private val context: Context) {

    private val platformNotificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun buildNotification(): Notification {
        if (shouldCreateNowPlayingChannel()) {
            createServerNotificationChannel()
        }

        val builder = NotificationCompat.Builder(context, SERVER_NOTIFICATION_CHANNEL)
        val title = context.resources.getString(R.string.notification_title)

        return builder
            .setSmallIcon(R.drawable.small_icon)
            .setContentTitle(applyBold(title))
            .setColor(ContextCompat.getColor(context, R.color.notification_color))
            .setContentText(context.resources.getText(R.string.notification_body))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .addAction(
                0,
                context.getString(R.string.shut_down),
                PendingIntent.getService(
                    context,
                    21,
                    Intent(
                        context,
                        PlainUpnpAndroidService::class.java
                    ).apply {
                        action = ACTION_EXIT
                    },
                    PendingIntent.FLAG_ONE_SHOT
                )
            )
            .build()
    }

    private fun applyBold(title: String) = SpannableString(title)
        .apply {
            setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                title.length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        }

    private fun shouldCreateNowPlayingChannel() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowPlayingChannelExists() =
        platformNotificationManager.getNotificationChannel(SERVER_NOTIFICATION_CHANNEL) != null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createServerNotificationChannel() {
        val notificationChannel = NotificationChannel(
            SERVER_NOTIFICATION_CHANNEL,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setShowBadge(false)
        }

        platformNotificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        internal const val SERVER_NOTIFICATION = 0x764B
        internal const val SERVER_NOTIFICATION_CHANNEL = "com.m3sv.plainupnp.server"
        internal const val ACTION_EXIT = "com.plainupnp.action.exit"
    }
}
