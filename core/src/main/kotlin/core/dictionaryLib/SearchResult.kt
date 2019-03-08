package com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib

import core.dictionaryLib.DictOriginType
import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(val searchTerm : String, val  results : List<Translation>)