package com.application.requiemproject.data.repository

import android.graphics.Bitmap
import com.application.requiemproject.model.TextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import com.google.android.gms.tasks.Tasks
import android.graphics.Rect

open class OCRRepository {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun recognizeText(bitmap: Bitmap, scale: Float, yOffset: Int): List<TextBlock> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = Tasks.await(recognizer.process(inputImage))

            result.textBlocks.map { block ->
                val box = block.boundingBox ?: Rect()

                val correctedBox = Rect(
                    (box.left / scale).toInt(),
                    ((box.top / scale) + yOffset).toInt(),
                    (box.right / scale).toInt(),
                    ((box.bottom / scale) + yOffset).toInt()
                )

                TextBlock(
                    text = block.text,
                    boundingBox = correctedBox
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}