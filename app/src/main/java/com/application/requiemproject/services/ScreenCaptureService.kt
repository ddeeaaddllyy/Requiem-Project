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
import android.util.Log
import android.widget.Toast
import com.application.requiemproject.data.api.RetrofitClient
import com.application.requiemproject.data.repository.OCRRepository
import com.application.requiemproject.data.repository.TranslationRepository
import com.application.requiemproject.managers.OverlayManager
import com.application.requiemproject.managers.ScreenCaptureManager
import com.application.requiemproject.notifications.NotificationActions
import com.application.requiemproject.notifications.NotificationChannelManager
import com.application.requiemproject.notifications.NotificationIds
import com.application.requiemproject.notifications.NotificationsFactory
import com.application.requiemproject.translator.MyMemoryTranslator
import com.application.requiemproject.utils.TagSet.SCREEN_CAPTURE_SERVICE_TAG
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
    private val translationRepository = TranslationRepository(
        MyMemoryTranslator(RetrofitClient.api)
    )

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
        captureManager.onProcessedCaptured = onProcessedCaptured@{ bitmap, scale, offset ->
            if (ocrJob?.isActive == true) {
                bitmap.recycle()
                return@onProcessedCaptured
            }

            ocrJob?.cancel()

            ocrJob = serviceScope.launch(Dispatchers.Default) {
                try {
                    val initialBlocks = ocrRepository.recognizeText(bitmap, scale, offset)
                    val translatedBlocks = translationRepository.translateBlocks(initialBlocks)

                    // Debug
                    Log.i(SCREEN_CAPTURE_SERVICE_TAG, "t_block: $translatedBlocks")

                    ensureActive()

                    withContext(Dispatchers.Main) {
                        overlayManager.updateTextOnScreen(translatedBlocks)
                    }

                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        Log.d(SCREEN_CAPTURE_SERVICE_TAG, "Error in onCreate(): ${e.message}")
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
            if (!isRunning) {
                captureManager.stopCapture()
                overlayManager.removeOverlay()
            }
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

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Toast.makeText(this, "You have low free memory. Be careful.", Toast.LENGTH_SHORT).show()
        Log.i(SCREEN_CAPTURE_SERVICE_TAG, "LOW MEMORY")

        // delete in future
        Log.e(SCREEN_CAPTURE_SERVICE_TAG, "OnLowMemory is active. We have to stop")
        onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        ocrJob?.cancel()
        captureManager.stopCapture()
        serviceScope.cancel()
        backgroundThread?.quitSafely()
        overlayManager.removeOverlay()
        try {
            backgroundThread?.join()
        } catch (e: InterruptedException) {
            Log.e(SCREEN_CAPTURE_SERVICE_TAG, "onDestroy call an error: ${e.message}\n${e.printStackTrace()}")
        }
    }

}