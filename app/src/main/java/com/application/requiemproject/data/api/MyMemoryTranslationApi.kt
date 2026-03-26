package com.application.requiemproject.data.api

import com.application.requiemproject.data.api.response.TranslationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MyMemoryTranslationApi {

    @GET("get")
    suspend fun getTranslateText(
        @Query("q") text: String,
        @Query("langpair") langPair: String,
        @Query("de") email: String = "very6igpen1s@gmail.com"
    ): Response<TranslationResponse>
}