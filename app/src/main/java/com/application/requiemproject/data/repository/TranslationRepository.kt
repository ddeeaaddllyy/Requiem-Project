package com.application.requiemproject.data.repository

import com.application.requiemproject.data.api.response.TranslationResult
import com.application.requiemproject.model.TextBlock
import com.application.requiemproject.model.TranslatorModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Repository responsible for translating text blocks using current translator.
 */
class TranslationRepository(
    private val currentTranslator: TranslatorModel
) {

    /**
     * Translates a list of [TextBlock] objects concurrently.
     *
     * @param blocks List of text blocks to translate.
     * @return List of translated text blocks.
     */
    suspend fun translateBlocks(blocks: List<TextBlock>): List<TextBlock> = coroutineScope {
        blocks.map { blocks ->
            async {
                val result = currentTranslator.translate(blocks.text, "en|ru")
                val outputText = when (result) {
                    is TranslationResult.Success -> result.text
                    is TranslationResult.Error -> blocks.text
                }

                blocks.copy(text = outputText)
            }
        }.awaitAll()
    }

}