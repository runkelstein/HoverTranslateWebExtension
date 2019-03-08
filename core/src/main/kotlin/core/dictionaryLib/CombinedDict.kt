package core.dictionaryLib

import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.DictCC
import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.IDictionary
import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.SearchResult
import core.storage.YandexSettings

class CombinedDict(private val dictionaries: List<IDictionary>) : IDictionary {

    constructor(dictCCText : String, yandexSettings: YandexSettings)
            : this(listOf(YandexDict(yandexSettings), DictCC(dictCCText)))

    override val description = "combined dictionary"

    override suspend fun findTranslations(word: String) =
         dictionaries
            .flatMap { it.findTranslations(word).results }
            .let { SearchResult(word, it) }

}