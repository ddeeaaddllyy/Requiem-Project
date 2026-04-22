package com.application.requiemproject.utils

import android.graphics.Rect
import com.application.requiemproject.model.TextBlock

object MergeText {
    private val textRegex = Regex(".*[\\p{L}\\p{N}].*")

    fun filterValidBlocks(blocks: List<TextBlock>): List<TextBlock> {
        return blocks.filter { it.text.isNotBlank() && it.text.matches(textRegex) }
    }

    fun mergeAndFilter(
        accBlocks: List<TextBlock>,
        ocrBlocks: List<TextBlock>
    ): List<TextBlock> {
        val validAcc = filterValidBlocks(accBlocks)
        val validOcr = filterValidBlocks(ocrBlocks)
        val result = validAcc.toMutableList()

        for (ocrBlock in validOcr) {
            val ocrRect = ocrBlock.boundingBox ?: continue

            val isOverlapping = validAcc.any { accBlock ->
                val accRect = accBlock.boundingBox ?: return@any false
                Rect.intersects(ocrRect, accRect)
            }

            if (!isOverlapping) {
                result.add(ocrBlock)
            }
        }

        return result
    }
}