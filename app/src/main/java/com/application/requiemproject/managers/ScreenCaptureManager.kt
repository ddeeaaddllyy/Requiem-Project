package com.application.requiemproject.managers

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import com.application.requiemproject.utils.ImageUtils

open class ScreenCaptureManager(
    private val context: Context,
    private val projectionManager: MediaProjectionManager,
    private val backgroundHandler: Handler
)
{
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    var onBitmapCaptured: ((android.graphics.Bitmap) -> Unit)? = null

    public fun startCapture(resultCode: Int, resultData: Intent) {
        mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)
        mediaProjection?.registerCallback(object  : MediaProjection.Callback() {
            override fun onStop() {
                Log.e("CAPTURE", "onStop")
            }
        }, backgroundHandler )

        backgroundHandler.postDelayed({
            virtualDisplay()
        }, 500)
    }

    private fun virtualDisplay() {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = windowManager.currentWindowMetrics
        val width = metrics.bounds.width()
        val height = metrics.bounds.height()
        val density = context.resources.displayMetrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, backgroundHandler
        )

        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                val bitmap = ImageUtils.imageToBitmap(image)
                image.close()

                if (bitmap != null) {
                    onBitmapCaptured?.invoke(bitmap)
                }
            }
        }, backgroundHandler)
    }

    open fun stopCapture() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
    }
}