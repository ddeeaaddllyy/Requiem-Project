package com.application.requiemproject.data.repository

import android.graphics.Bitmap
import com.application.requiemproject.model.TextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

open class OCRRepository {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun recognizeText(bitmap: Bitmap): List<TextBlock> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(inputImage).await()
            val detectedList = mutableListOf<TextBlock>()

            for (block in result.textBlocks) {
                detectedList.add(
                    TextBlock(
                        text = block.text,
                        boundingBox = block.boundingBox
                    )
                )
            }

            detectedList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}