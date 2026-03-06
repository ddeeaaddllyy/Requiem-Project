package com.application.requiemproject.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.View
import com.application.requiemproject.model.TextBlock
import androidx.core.graphics.toColorInt
import com.application.requiemproject.utils.TagSet.TEXT_OVERLAY_VIEW_TAG

class TextOverlayView(
    context: Context
): View(context)
{
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 45f
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        setShadowLayer(5f, 0f, 0f, Color.GRAY)
    }
    private val bgPaint = Paint().apply {
        color = "#80000000".toColorInt()
        style = Paint.Style.FILL
    }
    private var textBlock: List<TextBlock> = emptyList()

    fun setRect(newRect: List<TextBlock>) {
        this.textBlock = newRect
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (textBlock.isEmpty()) return

        for (block in textBlock) {
            try {
                val rect = block.boundingBox!!

                canvas.drawRect(rect, bgPaint)

                val textX = rect.centerX().toFloat()
                val textY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2

                canvas.drawText(block.text, textX, textY, textPaint)

            } catch (e: Exception) {
                Log.e(TEXT_OVERLAY_VIEW_TAG, "CRITICAL ERROR - onDraw said: $e")
            }

        }
    }
}