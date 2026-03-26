package com.application.requiemproject.translator

import android.util.Log
import com.application.requiemproject.data.api.MyMemoryTranslationApi
import com.application.requiemproject.model.TranslatorModel

class MyMemoryTranslator(
    private val api: MyMemoryTranslationApi
): TranslatorModel {
    val langpair = "en|ru" // delete in future
    override suspend fun translate(text: String): String? {
        return try {
            val response = api.getTranslateText(text, langpair)

            Log.d("API", "code: ${response.code()}")
            Log.d("API", "body: ${response.body()}")
            Log.d("API", "error: ${response.errorBody()?.string()}")

            if (response.isSuccessful) response.body()?.responseData?.translatedText else null

        } catch (e: Exception) {
            Log.e("MyMemoryTranslator", "hey? we've got an error: $e")
            throw e
        }
    }
}