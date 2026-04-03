package com.application.requiemproject.utils

import android.graphics.Rect
import com.application.requiemproject.model.TextBlock

object MergeText {
    fun mergeAndFilter(
        accBlocks: List<TextBlock>,
        ocrBlocks: List<TextBlock>
    ): List<TextBlock> {

        val validAcc = accBlocks.filter { it.text.isNotBlank() && !it.text.matches(Regex("^[^a-zA-Zа-яА-Я]+$")) }
        val validOcr = ocrBlocks.filter { it.text.isNotBlank() && !it.text.matches(Regex("^[^a-zA-Zа-яА-Я]+$")) }

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