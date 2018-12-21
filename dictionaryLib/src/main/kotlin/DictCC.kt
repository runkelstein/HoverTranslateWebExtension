package dictionaryLib

class DictCC(text : String) : Dictionary {

    private companion object {
        const val WHITESPACE = ' '
        const val TABULATOR = '\t'
        val SKIPELINECHARS = charArrayOf('#','(')
    }

    private val content : MutableMap<String, MutableList<Translation>> = HashMap()

    init {

        val contentLines : List<String>  = text.lines().filter { it.isNotEmpty() && !SKIPELINECHARS.contains(it[0])  }

        for (line in contentLines) {
            val key = line.takeWhile { it != WHITESPACE }

            val sourceLangText = line.takeWhile {  it != TABULATOR }
            val targetLangText = line.substring(sourceLangText.length+1).takeWhile { it != TABULATOR }

            if (!content.containsKey(key))
            {
                content[key] = ArrayList()
            }

            content[key]!!.add(Translation(sourceLangText, targetLangText))
        }
    }

    override fun findTranslations(word: String): SearchResult
    {
        val results = content[word]?.toList() ?: ArrayList()
        return SearchResult(word, results)
    }
}