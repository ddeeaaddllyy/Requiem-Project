package com.application.requiemproject.data.repository

import android.graphics.Bitmap
import com.application.requiemproject.model.TextBlock
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.android.gms.tasks.Tasks
import android.graphics.Rect
import android.util.Log
import com.application.requiemproject.utils.TagSet.OCR_REPOSITORY_TAG

open class OCRRepository {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    fun recognizeText(bitmap: Bitmap, scale: Float, yOffset: Int): List<TextBlock> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = Tasks.await(recognizer.process(inputImage))
            val characterList = mutableListOf<TextBlock>()

            for (block in result.textBlocks) {
                for (line in block.lines) {
                    val box = line.boundingBox ?: continue

                    val correctedBox = Rect(
                        (box.left / scale).toInt(),
                        ((box.top / scale) + yOffset).toInt(),
                        (box.right / scale).toInt(),
                        ((box.bottom / scale) + yOffset).toInt()
                    )

                    characterList.add(
                        TextBlock(
                            text = line.text,
                            boundingBox = correctedBox
                        )
                    )
                }
            }
            characterList
        } catch (e: Exception) {
            Log.e(OCR_REPOSITORY_TAG, "Recognize Text Exception: $e")
            e.printStackTrace()
            emptyList()
        }
    }

}