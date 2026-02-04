package com.application.requiemproject.services

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import com.application.requiemproject.data.repository.OCRRepository
import com.application.requiemproject.managers.ScreenCaptureManager
import com.application.requiemproject.notifications.NotificationActions
import com.application.requiemproject.notifications.NotificationChannelManager
import com.application.requiemproject.notifications.NotificationIds
import com.application.requiemproject.notifications.NotificationsFactory

open class ScreenCaptureService: Service() {

    // DEPENDENCIES
    private lateinit var captureManager: ScreenCaptureManager
    private val ocrRepository = OCRRepository()
    private lateinit var projectionManager: MediaProjectionManager

    // NOTIFICATIONS
    private lateinit var notificationsFactory: NotificationsFactory
    private lateinit var channelManager: NotificationChannelManager

    // THREADING
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null

    // STATE
    private var lastProcessTime = 0L
    private var isRunning = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startBackgroundThread()

        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        // notifications
        notificationsFactory = NotificationsFactory(context = this)
        channelManager = NotificationChannelManager(context = this)
        channelManager.createNotificationChannel()

        // main capture
        captureManager = ScreenCaptureManager(this, projectionManager, backgroundHandler!!)
        captureManager.onBitmapCaptured = { bitmap ->
            processCapturedBitmap(bitmap)

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        val notification = notificationsFactory.createNotification(isRunning)
        if (intent?.action == NotificationActions.TOGGLE_CAPTURE) {
            isRunning = !isRunning
            notificationsFactory.updateNotification(running = isRunning)
        }

        startForeground(
            NotificationIds.SCREEN_CAPTURE,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )

        val resultCode = intent?.getIntExtra("RESULT_CODE", -1) ?: -1
        val data = intent?.getParcelableExtra<Intent>("DATA")
        if (resultCode == -1 && data != null) {
            captureManager.startCapture(resultCode, data)
        }

        return START_NOT_STICKY
    }

    private fun processCapturedBitmap(bitmap: android.graphics.Bitmap) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessTime >= 1000) {
            lastProcessTime = currentTime

            ocrRepository.recognizeText(
                bitmap = bitmap,
                onResult = { text ->
                    if (text.isNotEmpty()) Log.e("TEXT_DETECTED", "Текст: $text")
                },
                onError = { error ->
                    Log.e("TEXT_ERROR", error)
                }
            )
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.stopCapture()
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

}