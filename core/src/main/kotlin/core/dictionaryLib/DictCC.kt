package com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib

import core.dictionaryLib.DictOriginType

class DictCC(text : String) : IDictionary {

    private companion object {
        const val WHITESPACE = ' '
        const val TABULATOR = '\t'
        val SKIPLINECHARS = charArrayOf('#','(')
    }

    private val content : MutableMap<String, MutableList<Translation>> = HashMap()

    override val description : String =
        text.lineSequence().first()
            .substringAfter(WHITESPACE)
            .substringBefore(TABULATOR)

    init {

        val contentLines = text.lineSequence()
            .filterNot { it.isEmpty() || SKIPLINECHARS.contains(it[0]) }

        // fill dictionary
        for (line in contentLines) {
            val key = line.substringBefore(WHITESPACE).substringBefore(TABULATOR)

            val sourceLangText = line.substringBefore(TABULATOR)
            val targetLangText = line.substringAfter(TABULATOR).substringBefore(TABULATOR)

            content.getOrPut(key) { ArrayList() }
                .add(Translation(sourceLangText, targetLangText, DictOriginType.DictCC))
        }

        // sort dictionary
        for (entry in content) {
            entry.value.sortWith(Comparator { a, b ->
                if (a.sourceLangText.length < b.sourceLangText.length) 0 else 1
            })
        }
    }

    override suspend fun findTranslations(word: String): SearchResult
    {
        val results = content[word]?.toList() ?: ArrayList()
        return SearchResult(word, results)
    }
}