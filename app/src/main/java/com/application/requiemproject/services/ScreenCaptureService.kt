package com.application.requiemproject.services

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.core.graphics.createBitmap
import com.application.requiemproject.notifications.NotificationActions
import com.application.requiemproject.notifications.NotificationChannelManager
import com.application.requiemproject.notifications.NotificationIds
import com.application.requiemproject.notifications.NotificationsFactory

open class ScreenCaptureService: Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var notificationsFactory: NotificationsFactory
    private lateinit var notificationChannelManager: NotificationChannelManager
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastProcessTime = 0L
    private var isRunning = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        notificationsFactory = NotificationsFactory(context = this)
        notificationChannelManager = NotificationChannelManager(context = this)
        notificationChannelManager.createNotificationChannel()
        startBackgroundThread()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = notificationsFactory.createNotification(isRunning)

        if (intent?.action == NotificationActions.TOGGLE_CAPTURE) {
            isRunning = !isRunning
            notificationsFactory.updateNotification(running = isRunning)
            return START_NOT_STICKY
        }

        startForeground(
            NotificationIds.SCREEN_CAPTURE,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )

        val resultCode = intent?.getIntExtra("RESULT_CODE", -1) ?: -1
        val data = intent?.getParcelableExtra<Intent>("DATA")

        if (resultCode == -1 && data != null) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)

            mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.e("SERVICE_DEBUG", "MediaProjection остановлен системой")
                }
            }, backgroundHandler)

            backgroundHandler?.postDelayed({
                setupVirtualDisplay()
            }, 500)
        }

        return START_NOT_STICKY
    }


    private fun setupVirtualDisplay() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val width: Int
        val height: Int
        val density: Int

        val windowMetrics = windowManager.currentWindowMetrics
        val bounds = windowMetrics.bounds
        width = bounds.width()
        height = bounds.height()
        density = resources.displayMetrics.densityDpi

        Log.e("SERVICE_DEBUG", "Размеры для VirtualDisplay: $width x $height, Density: $density")

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            backgroundHandler
        )

        imageReader?.setOnImageAvailableListener({ reader ->
            Log.d("SERVICE_DEBUG", "Кадр захвачен! Пытаюсь распознать...")
            val image = reader.acquireLatestImage()

            if (image != null) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastProcessTime >= 1000) {
                    lastProcessTime = currentTime
                    processImage(image)
                } else {
                    image.close()
                }
            }
        }, backgroundHandler)
    }

    private fun processImage(image: android.media.Image) {
        val bitmap = imageToBitmap(image)
        image.close()

        if (bitmap == null) return

        val inputImage = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val resultText = visionText.text
                if (resultText.isNotEmpty()) {
                    Log.e("TEXT_DETECTED", "Найден текст:\n$resultText")
                } else {
                    Log.e("TEXT_DETECTED", "Текст не найден")
                }
            }
            .addOnFailureListener { error ->
                Log.e("TEXT_ERROR", "Ошибка распознавания: ${error.message}")
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


    private fun imageToBitmap(image: android.media.Image): android.graphics.Bitmap? {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = createBitmap(image.width + rowPadding / pixelStride, image.height)
        bitmap.copyPixelsFromBuffer(buffer)

        return if (rowPadding != 0) {
            android.graphics.Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
        } else {
            bitmap
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBackgroundThread()
        virtualDisplay?.release()
        mediaProjection?.stop()
        imageReader?.close()
    }

}