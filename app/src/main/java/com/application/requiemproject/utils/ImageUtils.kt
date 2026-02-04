package com.application.requiemproject.utils

import androidx.core.graphics.createBitmap

public object ImageUtils {
    fun imageToBitmap(image: android.media.Image): android.graphics.Bitmap? {
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
}