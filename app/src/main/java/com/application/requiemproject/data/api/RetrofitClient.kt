package com.application.requiemproject.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Provides a singleton instance of Retrofit API client.
 *
 * Configured to work with MyMemory translation service.
 */
object RetrofitClient {
    private const val BASE_URL = "https://api.mymemory.translated.net/"

    /**
     * Lazily initialized API instance.
     *
     * Uses Gson for JSON deserialization.
     */
    val api: MyMemoryTranslationApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyMemoryTranslationApi::class.java)
    }
}