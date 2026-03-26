package com.application.requiemproject.model

import com.application.requiemproject.data.api.response.TranslationResult

/**
 * Represents a translation model used in the application.
 *
 * Implementations may use different translation providers (e.g., remote APIs,
 * local models, or third-party services).
 *
 * This abstraction allows users to switch between translation engines.
 */
interface TranslatorModel {
    /**
     * Translates input text into a target language.
     *
     * @param text Input text in any supported language.
     * @param languages Target language. It can come in different forms.
     * (e.g., MyMemory requires us to use the form: "en|ru")
     * @return Translated text or null if translation failed.
     * @throws java.io.IOException if network request fails.
     *
     *
     * This method may perform network requests.
     */
    suspend fun translate(text: String, languages: String): TranslationResult
}