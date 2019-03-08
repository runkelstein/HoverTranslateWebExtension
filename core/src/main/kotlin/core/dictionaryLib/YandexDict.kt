package core.dictionaryLib

import com.inspiritious.HoverTranslateWebExtension.core.cloud.yandex.YandexService
import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.IDictionary
import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.SearchResult
import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.Translation
import core.storage.YandexSettings

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
            return response.results.map { Translation(word, it,  DictOriginType.Yandex) }
        }
    }
}
