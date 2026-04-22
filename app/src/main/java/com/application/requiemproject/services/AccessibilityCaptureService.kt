package com.application.requiemproject.services

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import com.application.requiemproject.model.TextBlock
import java.util.ArrayDeque
import java.util.LinkedHashSet

class AccessibilityCaptureService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        AccessibilityTextProvider.latestBlocks = collectVisibleBlocks()
    }

    private fun collectVisibleBlocks(): List<TextBlock> {
        val uniqueKeys = LinkedHashSet<String>()
        val blocks = ArrayList<TextBlock>(64)

        windows
            .asSequence()
            .mapNotNull(AccessibilityWindowInfo::getRoot)
            .forEach { root ->
                collectWindowBlocks(root, uniqueKeys, blocks)
            }

        if (blocks.isNotEmpty()) {
            return blocks
        }

        val fallbackRoot = rootInActiveWindow ?: return emptyList()
        collectWindowBlocks(fallbackRoot, uniqueKeys, blocks)
        return blocks
    }

    private fun collectWindowBlocks(
        root: AccessibilityNodeInfo,
        uniqueKeys: MutableSet<String>,
        out: MutableList<TextBlock>
    ) {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (!node.isVisibleToUser) {
                continue
            }

            val rect = Rect()
            node.getBoundsInScreen(rect)
            if (!rect.isEmpty) {
                extractNodeTexts(node).forEach { text ->
                    val normalizedText = text.trim()
                    if (normalizedText.isEmpty()) return@forEach

                    val key = "${rect.left}:${rect.top}:${rect.right}:${rect.bottom}:$normalizedText"
                    if (uniqueKeys.add(key)) {
                        out += TextBlock(text = normalizedText, boundingBox = Rect(rect))
                    }
                }
            }

            for (index in 0 until node.childCount) {
                node.getChild(index)?.let(queue::addLast)
            }
        }
    }

    private fun extractNodeTexts(node: AccessibilityNodeInfo): List<String> {
        val values = ArrayList<CharSequence?>(6)
        values += node.text
        values += node.contentDescription

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            values += node.stateDescription
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            values += node.hintText
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            values += node.tooltipText
            values += node.paneTitle
        }

        return values
            .asSequence()
            .filterNotNull()
            .map(CharSequence::toString)
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
    }

    override fun onInterrupt() = Unit
}
