package com.application.requiemproject.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.View
import com.application.requiemproject.model.OverlayStyle
import com.application.requiemproject.model.TextBlock
import com.application.requiemproject.utils.TagSet.TEXT_OVERLAY_VIEW_TAG

class TextOverlayView(
    context: Context
): View(context)
{
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 45f
        style = Paint.Style.FILL
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        setShadowLayer(5f, 0f, 0f, Color.GRAY)
    }
    private val bgPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint().apply {
        color = Color.TRANSPARENT
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    private var textBlock: List<TextBlock> = emptyList()
    private var overlayStyle: OverlayStyle = OverlayStyle.TRANSLATION

    fun setOverlayStyle(style: OverlayStyle) {
        overlayStyle = style
        when (style) {
            OverlayStyle.TRANSLATION -> {
                bgPaint.color = Color.BLACK
                textPaint.color = Color.WHITE
                textPaint.setShadowLayer(5f, 0f, 0f, Color.GRAY)
                strokePaint.color = Color.TRANSPARENT
            }

            OverlayStyle.ACCESSIBILITY -> {
                bgPaint.color = Color.argb(55, 183, 28, 28)
                textPaint.color = Color.argb(255, 255, 210, 210)
                textPaint.setShadowLayer(3f, 0f, 0f, Color.BLACK)
                strokePaint.color = Color.argb(230, 255, 107, 107)
            }
        }
        invalidate()
    }

    fun setRect(newBlock: List<TextBlock>) {
        textBlock = newBlock
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (textBlock.isEmpty()) return

        for (block in textBlock) {
            val rect = block.boundingBox ?: continue
            val text = block.text

            try {
                canvas.drawRect(rect, bgPaint)
                if (overlayStyle == OverlayStyle.ACCESSIBILITY) {
                    canvas.drawRect(rect, strokePaint)
                }

                val heightFactor = if (overlayStyle == OverlayStyle.ACCESSIBILITY) 0.42f else 0.85f
                textPaint.textSize = rect.height() * heightFactor

                val textWidth = textPaint.measureText(text)
                if (textWidth > rect.width()) {
                    textPaint.textSize *= (rect.width().toFloat() / textWidth)
                }

                val textX = rect.centerX().toFloat()
                val fontMetrics = textPaint.fontMetrics
                val textY = rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2

                canvas.drawText(text, textX, textY, textPaint)

            } catch (e: Exception) {
                Log.e(TEXT_OVERLAY_VIEW_TAG, "CRITICAL ERROR - onDraw said: $e")
            }

        }
    }
}
