package core.dictionaryLib

class DictCC(text : String) : Dictionary {

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

        for (line in contentLines) {
            val key = line.substringBefore(WHITESPACE)

            val sourceLangText = line.substringBefore(TABULATOR)
            val targetLangText = line.substringAfter(TABULATOR).substringBefore(TABULATOR)

            content.getOrPut(key) { ArrayList() }
                .add(Translation(sourceLangText, targetLangText))
        }
    }

    override fun findTranslations(word: String): SearchResult
    {
        val results = content[word]?.toList() ?: ArrayList()
        return SearchResult(word, results)
    }
}