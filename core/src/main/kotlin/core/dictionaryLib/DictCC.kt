package com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib

class DictCC(text : String) : IDictionary {

    private companion object {
        const val WHITESPACE = ' '
        const val TABULATOR = '\t'
        val SKIPELINECHARS = charArrayOf('#','(')
    }

    private val content : MutableMap<String, MutableList<Translation>> = HashMap()

    override val description : String =
        text.lineSequence().first()
            .substringAfter(WHITESPACE)
            .substringBefore(TABULATOR)

    init {

        val contentLines = text.lineSequence()
            .filterNot { it.isEmpty() || SKIPELINECHARS.contains(it[0]) }

        // fill dictionary
        for (line in contentLines) {
            val key = line.substringBefore(WHITESPACE)

            val sourceLangText = line.substringBefore(TABULATOR)
            val targetLangText = line.substringAfter(TABULATOR).substringBefore(TABULATOR)

            content.getOrPut(key) { ArrayList() }
                .add(Translation(sourceLangText, targetLangText))
        }

        // sort dictionary
        for (entry in content) {
            entry.value.sortWith(Comparator { a, b ->
                if (a.sourceLangText.length < b.sourceLangText.length) 0 else 1
            })
        }
    }

    override fun findTranslations(word: String): SearchResult
    {
        val results = content[word]?.toList() ?: ArrayList()
        return SearchResult(word, results)
    }
}