package com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib

import com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib.DictCC
import com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib.IDictionary
import com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib.SearchResult
import com.runkelstein.hoverTranslateWebExtension.core.storage.YandexSettings

class CombinedDict(private val dictionaries: List<IDictionary>) : IDictionary {

    constructor(dictCCText : String, yandexSettings: YandexSettings)
            : this(listOf(YandexDict(yandexSettings), DictCC(dictCCText)))

    override val description = "combined dictionary"

    override suspend fun findTranslations(word: String) =
         dictionaries
            .flatMap { it.findTranslations(word).results }
            .let { SearchResult(word, it) }

}