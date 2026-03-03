package com.application.requiemproject.managers

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import com.application.requiemproject.utils.ImagePreprocessor

open class ScreenCaptureManager(
    private val context: Context,
    private val projectionManager: MediaProjectionManager,
    private val backgroundHandler: Handler
) {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var lastProcessTime: Long = 0L
    private val minIntervalMs: Long = 2000L

    private var screenWidth = 0
    private var screenHeight = 0
    private var density = 0

    var onBitmapCaptured: ((Bitmap) -> Unit)? = null
    var onProcessedCaptured: ((Bitmap, Float, Int) -> Unit)? = null

    private val scaleFactor = 1.5f


    fun startCapture(resultCode: Int, resultData: Intent) {
        mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)

        mediaProjection?.registerCallback(object: MediaProjection.Callback() {
            override fun onStop() {
                Log.d("ScreenCaptureManager", "fun startCapture was stopped")
                stopCapture()
            }
        }, backgroundHandler)

        setupVirtualDisplay()
    }

    private fun setupVirtualDisplay() {

        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val metrics = windowManager.currentWindowMetrics
        val bounds = metrics.bounds

        screenWidth = bounds.width()
        screenHeight = bounds.height()
        density = context.resources.displayMetrics.densityDpi

        imageReader?.close()

        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            backgroundHandler
        )

        imageReader?.setOnImageAvailableListener(
            imageListener,
            backgroundHandler
        )
    }

    private val imageListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage() ?: return@OnImageAvailableListener

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessTime < minIntervalMs) {
            image.close()
            return@OnImageAvailableListener
        }
        lastProcessTime = currentTime

        val rawBitmap = imageToBitmapSafe(image)
        image.close()

        val statusBarHeight = calculateStatusBarHeight()
        val cropped = cropBitmap(rawBitmap, statusBarHeight)

        val processed = ImagePreprocessor.prepare(cropped, scaleFactor)

        onProcessedCaptured?.invoke(processed, scaleFactor, statusBarHeight)
    }

    private fun calculateStatusBarHeight(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = windowManager.currentWindowMetrics
        return metrics.windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.statusBars()
        ).top
    }

    private fun cropBitmap(bitmap: Bitmap, offset: Int): Bitmap {
        if (offset <= 0 || offset >= bitmap.height) return bitmap
        return createBitmap(bitmap, 0, offset, bitmap.width, bitmap.height - offset)
    }

    private fun imageToBitmapSafe(image: Image): Bitmap {

        val plane = image.planes[0]
        val buffer = plane.buffer

        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * screenWidth

        val bitmap = createBitmap(
            screenWidth + rowPadding / pixelStride,
            screenHeight,
            Bitmap.Config.ARGB_8888
        )

        bitmap.copyPixelsFromBuffer(buffer)

        return cropStatusBar(bitmap)
    }

    private fun cropStatusBar(bitmap: Bitmap): Bitmap {

        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val metrics = windowManager.currentWindowMetrics
        val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.statusBars()
        )

        val statusBarHeight = insets.top

        if (statusBarHeight <= 0 || statusBarHeight >= bitmap.height) {
            return bitmap
        }

        val safeHeight = bitmap.height - statusBarHeight

        return createBitmap(
            bitmap,
            0,
            statusBarHeight,
            bitmap.width,
            safeHeight
        )
    }

    open fun stopCapture() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()

        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }
}