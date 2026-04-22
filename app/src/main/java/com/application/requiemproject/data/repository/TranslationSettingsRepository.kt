package com.application.requiemproject.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.application.requiemproject.model.AppLanguage
import com.application.requiemproject.model.ScanSource
import com.application.requiemproject.model.TranslationSettings

class TranslationSettingsRepository(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getSettings(): TranslationSettings {
        val source = preferences.getString(KEY_SOURCE_LANGUAGE, AppLanguage.defaultSource.name)
        val target = preferences.getString(KEY_TARGET_LANGUAGE, AppLanguage.defaultTarget.name)
        val scanSource = preferences.getString(KEY_SCAN_SOURCE, ScanSource.OCR.name)

        return TranslationSettings(
            sourceLanguage = AppLanguage.entries.firstOrNull { it.name == source }
                ?: AppLanguage.defaultSource,
            targetLanguage = AppLanguage.entries.firstOrNull { it.name == target }
                ?: AppLanguage.defaultTarget,
            scanSource = ScanSource.entries.firstOrNull { it.name == scanSource }
                ?: ScanSource.OCR
        )
    }

    fun updateSourceLanguage(language: AppLanguage) {
        preferences.edit { putString(KEY_SOURCE_LANGUAGE, language.name) }
    }

    fun updateTargetLanguage(language: AppLanguage) {
        preferences.edit { putString(KEY_TARGET_LANGUAGE, language.name) }
    }

    fun updateScanSource(scanSource: ScanSource) {
        preferences.edit { putString(KEY_SCAN_SOURCE, scanSource.name) }
    }

    companion object {
        private const val PREFS_NAME = "translation_settings"
        private const val KEY_SOURCE_LANGUAGE = "source_language"
        private const val KEY_TARGET_LANGUAGE = "target_language"
        private const val KEY_SCAN_SOURCE = "scan_source"
    }
}
