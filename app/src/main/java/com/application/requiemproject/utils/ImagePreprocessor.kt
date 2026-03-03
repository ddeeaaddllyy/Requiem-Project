package com.application.requiemproject.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object ImagePreprocessor {
    fun prepare(bitmap: Bitmap, scaleFactor: Float = 1.5f): Bitmap {
        // Convert to grayscale and increase contrast
        val grayscaleBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()

        val colorMatrix = ColorMatrix().apply {
            setSaturation(0f) // Grayscale
            // Increase contrast to remove noises
            val contrast = 1.5f
            val brightness = -20f
            val array = floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
            postConcat(ColorMatrix(array))
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // upscale 1.5x
        val width = (grayscaleBitmap.width * scaleFactor).toInt()
        val height = (grayscaleBitmap.height * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(grayscaleBitmap, width, height, true)
    }
}