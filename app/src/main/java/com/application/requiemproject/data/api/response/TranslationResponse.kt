package com.application.requiemproject.data.api.response

import com.google.gson.annotations.SerializedName

data class TranslationResponse(
    @SerializedName("responseData") val responseData: ResponseData?
)
data class ResponseData(
    @SerializedName("translatedText") val translatedText: String?
)