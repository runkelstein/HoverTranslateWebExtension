package com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib

interface IDictionary
{
    val description : String

    suspend fun findTranslations(word : String) : SearchResult
}