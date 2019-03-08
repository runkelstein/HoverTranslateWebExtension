package com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib

interface IDictionary
{
    val description : String

    suspend fun findTranslations(word : String) : SearchResult
}