package com.application.requiemproject.model

data class TranslationSettings(
    val sourceLanguage: AppLanguage,
    val targetLanguage: AppLanguage,
    val scanSource: ScanSource
) {
    val languagePair: String
        get() = "${sourceLanguage.translationCode}|${targetLanguage.translationCode}"
}
