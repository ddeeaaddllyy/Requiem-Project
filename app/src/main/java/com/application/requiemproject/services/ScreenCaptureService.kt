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
import com.application.requiemproject.App
import com.application.requiemproject.data.repository.OCRRepository
import com.application.requiemproject.data.repository.TranslationRepository
import com.application.requiemproject.managers.OverlayManager
import com.application.requiemproject.managers.ScreenCaptureManager
import com.application.requiemproject.model.TextBlock
import com.application.requiemproject.model.TranslatorModel
import com.application.requiemproject.notifications.NotificationActions
import com.application.requiemproject.notifications.NotificationChannelManager
import com.application.requiemproject.notifications.NotificationIds
import com.application.requiemproject.notifications.NotificationsFactory
import com.application.requiemproject.utils.MergeText
import com.application.requiemproject.utils.TagSet.SCREEN_CAPTURE_SERVICE_TAG
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class ScreenCaptureService: Service() {

    companion object {
        const val EXTRA_USE_ACCESSIBILITY_MODE = "EXTRA_USE_ACCESSIBILITY_MODE"
    }

    // DEPENDENCIES
    private lateinit var captureManager: ScreenCaptureManager
    private val ocrRepository = OCRRepository()
    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var overlayManager: OverlayManager
    private lateinit var translator: TranslatorModel
    private lateinit var translationRepository: TranslationRepository

    // NOTIFICATIONS
    private lateinit var notificationsFactory: NotificationsFactory
    private lateinit var channelManager: NotificationChannelManager

    // THREADING
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // STATE
    private var isRunning: Boolean = true
    private var ocrJob: Job? = null
    private var lastFrameBlocks: List<TextBlock> = emptyList()
    private var useAccessibilityMode: Boolean = false

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
        translator = (application as App).myMemoryTranslator
        translationRepository = TranslationRepository(translator)
        captureManager = ScreenCaptureManager(this, projectionManager, backgroundHandler!!)
        captureManager.onProcessedCaptured = onProcessedCaptured@{ bitmap, scale, offset ->

            if (ocrJob?.isActive == true) {
                bitmap.recycle()
                return@onProcessedCaptured
            }

            ocrJob = serviceScope.launch {
                try {
                    val ocrBlocks = ocrRepository.recognizeText(bitmap, scale, offset)
                    val mergedBlocks = if (useAccessibilityMode) {
                        val accBlocks = AccessibilityTextProvider.latestBlocks
                        MergeText.mergeAndFilter(accBlocks, ocrBlocks)
                    } else {
                        MergeText.mergeAndFilter(emptyList(), ocrBlocks)
                    }

                    if (mergedBlocks.isEmpty()) {
                        withContext(Dispatchers.Main) { overlayManager.updateTextOnScreen(emptyList()) }
                        return@launch
                    }

                    if (mergedBlocks == lastFrameBlocks) return@launch
                    lastFrameBlocks = mergedBlocks

                    val translatedBlocks = translationRepository.translateBlocks(mergedBlocks)

                    withContext(Dispatchers.Main) {
                        overlayManager.updateTextOnScreen(translatedBlocks) // [cite: 14]
                    }

                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        Log.e(SCREEN_CAPTURE_SERVICE_TAG, "Pipeline Error: ${e.message}") // [cite: 15]
                    }
                } finally {
                    bitmap.recycle()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.hasExtra(EXTRA_USE_ACCESSIBILITY_MODE) == true) {
            useAccessibilityMode = intent.getBooleanExtra(EXTRA_USE_ACCESSIBILITY_MODE, false)
        }

        val notification = notificationsFactory.createNotification(isRunning)
        if (intent?.action == NotificationActions.TOGGLE_CAPTURE) {
            isRunning = !isRunning
            notificationsFactory.updateNotification(running = isRunning)
            if (!isRunning) {
                captureManager.stopCapture()
                overlayManager.removeOverlay()
            } else if (useAccessibilityMode) {
                overlayManager.showWorkingBubble()
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
            @Suppress("deprecation")
            intent?.getParcelableExtra("DATA")
        }


        if (resultCode == -1 && data != null) {
            captureManager.startCapture(resultCode, data)
            overlayManager.showOverlay()
            if (useAccessibilityMode) {
                overlayManager.showWorkingBubble()
            } else {
                overlayManager.hideWorkingBubble()
            }
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
        Log.e(SCREEN_CAPTURE_SERVICE_TAG, "OnLowMemory is active. We have to stop")
        stopSelf()
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
