package com.application.requiemproject.services

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import com.application.requiemproject.data.repository.OCRRepository
import com.application.requiemproject.managers.OverlayManager
import com.application.requiemproject.managers.ScreenCaptureManager
import com.application.requiemproject.notifications.NotificationActions
import com.application.requiemproject.notifications.NotificationChannelManager
import com.application.requiemproject.notifications.NotificationIds
import com.application.requiemproject.notifications.NotificationsFactory
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class ScreenCaptureService: Service() {

    // DEPENDENCIES
    private lateinit var captureManager: ScreenCaptureManager
    private val ocrRepository = OCRRepository()
    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var overlayManager: OverlayManager

    // NOTIFICATIONS
    private lateinit var notificationsFactory: NotificationsFactory
    private lateinit var channelManager: NotificationChannelManager

    // THREADING
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // STATE
    private var isRunning = true
    private var ocrJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startBackgroundThread()

        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        overlayManager = OverlayManager(applicationContext)

        // notifications
        notificationsFactory = NotificationsFactory(context = this)
        channelManager = NotificationChannelManager(context = this)
        channelManager.createNotificationChannel()

        // main capture
        captureManager = ScreenCaptureManager(this, projectionManager, backgroundHandler!!)
        captureManager.onProcessedCaptured = { bitmap, scale, offset ->
            ocrJob?.cancel()


            ocrJob = serviceScope.launch(Dispatchers.Default) {
                try {
                    val textBlocks = ocrRepository.recognizeText(bitmap, scale, offset)

                    ensureActive()

                    withContext(Dispatchers.Main) {
                        overlayManager.updateTextOnScreen(textBlocks)
                    }
                } finally {
                    bitmap.recycle()
                }
            }
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

        val resultCode = intent?.getIntExtra("RESULT_CODE", Activity.RESULT_CANCELED)
            ?: Activity.RESULT_CANCELED
        val data = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("DATA", Intent::class.java)
        } else {
            intent?.getParcelableExtra("DATA")
        }


        if (resultCode == -1 && data != null) {
            captureManager.startCapture(resultCode, data)
            overlayManager.showOverlay()
        }

        return START_NOT_STICKY
    }

//    private suspend fun processBitmap(bitmap: android.graphics.Bitmap) {
//        val text = ocrRepository.recognizeText(bitmap)
//
//        withContext(Dispatchers.Main) {
//            overlayManager.updateTextOnScreen(text)
//        }
//    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // in future
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.stopCapture()
        serviceScope.cancel()
        backgroundThread?.quitSafely()
        overlayManager.removeOverlay()
        try {
            backgroundThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

}