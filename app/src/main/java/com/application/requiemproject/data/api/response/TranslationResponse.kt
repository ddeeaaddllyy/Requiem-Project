package com.application.requiemproject.data.api.response

import com.google.gson.annotations.SerializedName

/**
 * Root response from MyMemory API.
 *
 * @property responseData Contains translation result data.
 */
data class TranslationResponse(
    @field:SerializedName("responseData") val responseData: ResponseData?
)

/**
 * Contains translated text returned by the API.
 *
 * @property translatedText Result of translation.
 * May be null if translation failed or response is incomplete.
 */
data class ResponseData(
    @field:SerializedName("translatedText") val translatedText: String?
)