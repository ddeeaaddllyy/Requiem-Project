package com.application.requiemproject.services

import com.application.requiemproject.model.TextBlock
import kotlin.concurrent.Volatile

object AccessibilityTextProvider {
    @Volatile
    var latestBlocks: List<TextBlock> = emptyList()
}