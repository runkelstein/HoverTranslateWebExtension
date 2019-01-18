package com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib

interface IDictionary
{
    val description : String

    fun findTranslations(word : String) : SearchResult
}