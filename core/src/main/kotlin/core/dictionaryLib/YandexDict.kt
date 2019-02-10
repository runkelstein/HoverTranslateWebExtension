package core.dictionaryLib

import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.IDictionary
import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.SearchResult

class YandexDict() : IDictionary {

    override val description : String = "GoogleTranslate"

    override fun findTranslations(word: String): SearchResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}