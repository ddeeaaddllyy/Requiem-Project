package com.application.requiemproject.translator

import android.util.Log
import com.application.requiemproject.data.api.MyMemoryTranslationApi
import com.application.requiemproject.data.api.response.TranslationResult
import com.application.requiemproject.data.local.dao.UserDao
import com.application.requiemproject.managers.SessionManager
import com.application.requiemproject.model.TranslatorModel

/**
 * Implementation of [TranslatorModel] that uses the MyMemory translation API.
 *
 * This translator performs network requests to fetch translated text.
 * It supports dynamic language selection via a language pair string.
 *
 * Example of language pair: "en|ru" (English -> Russian)
 *
 * @property api Retrofit API used to communicate with MyMemory service.
 * @property userDao from [com.application.requiemproject.data.local.db.AppDatabase] used to MyMemory API for more tokens for translate
 * @property sessionManager from [SessionManager] used to get user id.
 */
class MyMemoryTranslator(
    private val api: MyMemoryTranslationApi,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
): TranslatorModel {
    /**
     * Translates given [text] using MyMemory API.
     *
     * @param text Text to translate.
     * @param languages Language pair in format "source|target"
     * (e.g., "en|ru").
     *
     * @return [TranslationResult.Success] with translated text if successful,
     * or [TranslationResult.Error] if something went wrong.
     *
     * Possible failure reasons:
     * - Network error
     * - API error response
     * - Empty or invalid response body
     */
    override suspend fun translate(text: String, languages: String): TranslationResult {
        return try {
            val userId = sessionManager.getUserId()
            val email = userDao.getEmailById(userId)
            val response = api.getTranslateText(
                text,
                languages,
                email
            )

            if (!response.isSuccessful) {
                return TranslationResult.Error(
                    message = response.errorBody()?.string() ?: "Unknown api error"
                )
            }

            val translatedText = response.body()?.responseData?.translatedText

            if (translatedText.isNullOrBlank()) {
                return TranslationResult.Error(
                    message = "Empty response"
                )
            }

            TranslationResult.Success(
                text = translatedText
            )

        } catch (e: Exception) {
            Log.e("MyMemoryTranslator", "Exception during translation", e)
            TranslationResult.Error(
                message = e.message ?: "Unknown error"
            )
        }
    }
}