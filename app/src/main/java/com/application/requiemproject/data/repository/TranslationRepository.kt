package com.application.requiemproject.data.repository

import com.application.requiemproject.model.TextBlock
import com.application.requiemproject.model.TranslatorModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class TranslationRepository(
    private val currentTranslator: TranslatorModel
) {
    suspend fun translateBlocks(blocks: List<TextBlock>): List<TextBlock> = coroutineScope {
        blocks.map { blocks ->
            async {
                val translated = currentTranslator.translate(blocks.text)
                blocks.copy(text = translated ?: blocks.text)
            }
        }.awaitAll()
    }

}