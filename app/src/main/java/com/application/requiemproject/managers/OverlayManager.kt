package com.application.requiemproject.managers

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import com.application.requiemproject.model.TextBlock
import com.application.requiemproject.view.TextOverlayView

class OverlayManager(
    private val context: Context
)
{
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: FrameLayout? = null
    private var drawView: TextOverlayView? = null

    fun showOverlay() {
        if (!Settings.canDrawOverlays(context)) {
            Log.e("OverlayManager", "CRITICAL ERROR: application can't draw overlays (most likely it doesn't have 'Settings.canDrawOverlays' permission)")
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

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            Log.e("OverlayManager", "Critical overlay error in showOverlay(): ${e.message}")
            e.printStackTrace()
        }
    }

    fun updateTextOnScreen(rects: List<TextBlock>) {
        drawView?.post {
            drawView?.setRect(rects)
        }

    }

    fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                Log.e("OverlayManager", "Critical overlay error in removeOverlay(): ${e.message}")
                e.printStackTrace()
            }
        }
        drawView?.setRect(emptyList())
        overlayView = null
        drawView = null
    }
}