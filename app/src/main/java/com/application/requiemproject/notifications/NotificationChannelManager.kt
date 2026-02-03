package com.application.requiemproject.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE

open class NotificationChannelManager(
    private val context: Context
)
{
    open fun createNotificationChannel() {
        val channelName = "Screen Capture Service"
        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH)
            .apply {
                description = "description"
            }

        val notificationManager: NotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "screen_capture_channel"
    }
}