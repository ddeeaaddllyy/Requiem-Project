package com.application.requiemproject.model

enum class AppLanguage(
    val displayName: String,
    val translationCode: String
) {
    ENGLISH("English", "en"),
    RUSSIAN("Russian", "ru"),
    JAPANESE("Japanese", "ja"),
    GERMAN("German", "de"),
    FRENCH("French", "fr");

    companion object {
        val defaultSource = ENGLISH
        val defaultTarget = RUSSIAN

        fun fromDisplayName(
            value: String?,
            fallback: AppLanguage = defaultSource
        ): AppLanguage {
            return entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) }
                ?: fallback
        }
    }
}
