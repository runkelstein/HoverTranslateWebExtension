package dictionaryLib

interface Dictionary
{
    fun findTranslations(word : String) : SearchResult
}