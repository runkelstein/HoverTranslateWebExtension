package core.dictionaryLib

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(val searchTerm : String, val  results : List<Translation>)