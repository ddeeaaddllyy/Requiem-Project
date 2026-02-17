package com.application.requiemproject.model

import android.graphics.Rect

data class TextBlock(
    val text: String,
    val boundingBox: Rect?
)