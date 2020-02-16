package com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib

import com.runkelstein.hoverTranslateWebExtension.core.cloud.yandex.YandexService
import com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib.IDictionary
import com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib.SearchResult
import com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib.Translation
import com.runkelstein.hoverTranslateWebExtension.core.storage.YandexSettings

class YandexDict(private val settings : YandexSettings) : IDictionary {

    override val description : String = "Yandex"

    private val content : MutableMap<String, List<Translation>> = HashMap()

    override suspend fun findTranslations(word: String): SearchResult {
        val translations = fetchFromCacheOrWeb(word)
        return SearchResult(word, translations)
    }

    private suspend fun fetchFromCacheOrWeb(word : String) : List<Translation> {

        return content.getOrPut(word) {
            val response = YandexService.translate(word, settings.sourceLang, settings.targetLang)
            return response.results.map { Translation(word, it,
                DictOriginType.Yandex
            ) }
        }
    }
}
