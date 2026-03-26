package com.application.requiemproject.model

interface TranslatorModel { suspend fun translate(text: String): String? }