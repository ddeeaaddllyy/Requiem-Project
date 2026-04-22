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
import com.application.requiemproject.model.OverlayStyle
import com.application.requiemproject.utils.TagSet.OVERLAY_MANAGER_TAG
import com.application.requiemproject.view.TextOverlayView

class OverlayManager(
    private val context: Context
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: FrameLayout? = null
    private var translationView: TextOverlayView? = null
    private var accessibilityView: TextOverlayView? = null

    fun showOverlay() {
        if (!Settings.canDrawOverlays(context)) {
            Log.e(
                OVERLAY_MANAGER_TAG,
                "CRITICAL ERROR: application can't draw overlays (most likely it doesn't have 'Settings.canDrawOverlays' permission)"
            )
            return
        }

        if (overlayView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        overlayView = FrameLayout(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        translationView = TextOverlayView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setOverlayStyle(OverlayStyle.TRANSLATION)
        }
        overlayView?.addView(translationView, layoutParams)

        accessibilityView = TextOverlayView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setOverlayStyle(OverlayStyle.ACCESSIBILITY)
        }
        overlayView?.addView(accessibilityView, layoutParams)

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            Log.e(OVERLAY_MANAGER_TAG, "Critical overlay error in showOverlay(): ${e.message}")
        }
    }

    fun updateTextOnScreen(rects: List<TextBlock>) {
        translationView?.post {
            translationView?.setRect(rects)
        }
    }

    fun updateAccessibilityOverlay(rects: List<TextBlock>) {
        accessibilityView?.post {
            accessibilityView?.setRect(rects)
        }
    }

    fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                Log.e(OVERLAY_MANAGER_TAG, "Critical overlay error in removeOverlay(): ${e.message}")
            }
        }

        translationView?.setRect(emptyList())
        accessibilityView?.setRect(emptyList())
        translationView = null
        accessibilityView = null
        overlayView = null
    }
}
