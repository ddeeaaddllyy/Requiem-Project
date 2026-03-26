package com.application.requiemproject.data.api

import com.application.requiemproject.data.api.response.TranslationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API for interacting with the MyMemory translation service.
 *
 * Documentation: [MyMemory Docs](https://mymemory.translated.net/doc/spec.php)
 *
 * This API provides access to machine translation via HTTP requests.
 */
interface MyMemoryTranslationApi {

    /**
     * Requests translation from MyMemory service.
     *
     * @param text Text to translate.
     * @param langPair Language pair in format "source|target"
     * (e.g., "en|ru", "de|en").
     * @param email Optional email used for identification by the API.
     *
     * @return [Response] containing [TranslationResponse] if successful.
     *
     * This method performs a network request and may throw exceptions
     * related to connectivity issues.
     */
    @GET("get")
    suspend fun getTranslateText(
        @Query("q") text: String,
        @Query("langpair") langPair: String,
        @Query("de") email: String = "very6igpen1s@gmail.com"
    ): Response<TranslationResponse>
}