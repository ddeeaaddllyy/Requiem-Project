package com.application.requiemproject.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import com.application.requiemproject.R
import com.application.requiemproject.services.ScreenCaptureService

open class NotificationsFactory(
    private val context: Context
)
{
    open fun createNotification(isRunning: Boolean): Notification {
        val title = "Screen capture service"
        val text = if (isRunning) {
            "Recording is running"
        } else {
            "Recording is stopped"
        }

        val actionTitle = if (isRunning) "Stop" else "Start"

        return Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_24)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_notification_24,
                actionTitle,
                toggleIntent()
            )
            .build()
    }

    open fun updateNotification(running: Boolean) {
        val notification = createNotification(running)
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationIds.SCREEN_CAPTURE, notification)
    }

    private fun toggleIntent(): PendingIntent {
        val intent = Intent(context, ScreenCaptureService::class.java).apply {
            action = NotificationActions.TOGGLE_CAPTURE
        }

        return PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val CHANNEL_ID = "screen_capture_channel"
    }
}