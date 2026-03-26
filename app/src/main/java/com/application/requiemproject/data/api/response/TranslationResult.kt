package com.application.requiemproject.data.api.response

/**
 * Represents the outcome of a translation operation.
 *
 * This sealed class models the two possible results of translating text:
 * - [Success]: the translation succeeded and returns the translated text.
 * - [Error]: the translation failed and returns an error message.
 *
 * Using a sealed class ensures exhaustive handling in when expressions,
 * making the code safer and more maintainable.
 *
 * @see Success
 * @see Error
 * @since 1.0.0
 */
sealed class TranslationResult {

    /**
     * Indicates a successful translation.
     *
     * @property text The translated text result.
     */
    data class Success(val text: String): TranslationResult()

    /**
     * Indicates a failure during translation.
     *
     * @property message A human-readable description of the error.
     */
    data class Error(val message: String): TranslationResult()
}