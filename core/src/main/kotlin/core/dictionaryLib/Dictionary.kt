package core.dictionaryLib

interface Dictionary
{
    val description : String

    fun findTranslations(word : String) : SearchResult
}