package com.application.requiemproject.managers

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.util.TypedValue
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.application.requiemproject.model.TextBlock
import com.application.requiemproject.view.TextOverlayView
import com.application.requiemproject.utils.TagSet.OVERLAY_MANAGER_TAG

class OverlayManager(
    private val context: Context
)
{
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: FrameLayout? = null
    private var drawView: TextOverlayView? = null
    private var statusBubble: TextView? = null
    private var shouldShowBubble: Boolean = false

    fun showOverlay() {
        if (!Settings.canDrawOverlays(context)) {
            Log.e(OVERLAY_MANAGER_TAG, "CRITICAL ERROR: application can't draw overlays (most likely it doesn't have 'Settings.canDrawOverlays' permission)")
            return
        }

        if (overlayView != null) return

        val layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        overlayView = FrameLayout(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }

        drawView = TextOverlayView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }

        val drawParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        overlayView?.addView(drawView, drawParams)

        statusBubble = createStatusBubble()
        overlayView?.addView(statusBubble, createStatusBubbleLayoutParams())
        updateBubbleVisibility()

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            Log.e(OVERLAY_MANAGER_TAG, "Critical overlay error in showOverlay(): ${e.message}")
            e.printStackTrace()
        }
    }

    fun updateTextOnScreen(rects: List<TextBlock>) {
        drawView?.post {
            drawView?.setRect(rects)
        }

    }

    fun showWorkingBubble() {
        shouldShowBubble = true
        updateBubbleVisibility()
    }

    fun hideWorkingBubble() {
        shouldShowBubble = false
        updateBubbleVisibility()
    }

    fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                Log.e(OVERLAY_MANAGER_TAG, "Critical overlay error in removeOverlay(): ${e.message}")
                e.printStackTrace()
            }
        }
        drawView?.setRect(emptyList())
        overlayView = null
        drawView = null
        statusBubble = null
        shouldShowBubble = false
    }

    private fun createStatusBubble(): TextView {
        return TextView(context).apply {
            text = "Accessibility + OCR"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(18).toFloat()
                setColor(Color.parseColor("#CCB71C1C"))
            }
            elevation = dpToPx(6).toFloat()
            alpha = 0.95f
        }
    }

    private fun createStatusBubbleLayoutParams(): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = dpToPx(24)
            marginEnd = dpToPx(24)
        }
    }

    private fun updateBubbleVisibility() {
        statusBubble?.visibility = if (shouldShowBubble) View.VISIBLE else View.GONE
    }

    private fun dpToPx(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
