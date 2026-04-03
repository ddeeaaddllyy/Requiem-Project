package com.application.requiemproject.services

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.application.requiemproject.model.TextBlock

/**
 * Documentation will be available here in the future.
 */
class AccessibilityCaptureService: AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return

        val blocks = mutableListOf<TextBlock>()
        collectText(root, blocks)

        AccessibilityTextProvider.latestBlocks = blocks

        root.recycle()
    }

    private fun collectText(node: AccessibilityNodeInfo, out: MutableList<TextBlock>) {
        if (!node.text.isNullOrBlank()) {
            val rect = Rect()
            node.getBoundsInScreen(rect)

            if (!rect.isEmpty) {
                out.add(TextBlock(text = node.text.toString(), boundingBox = rect))
            }
        }

        for (index in 0 until node.childCount) {
            node.getChild(index)?.let {
                collectText(it, out)
                it.recycle()
            }
        }
    }

    override fun onInterrupt() = Unit
}