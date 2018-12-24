package core.dictionaryLib

import kotlinx.serialization.Serializable

@Serializable
data class Translation(val sourceLangText : String, val targetLangText : String)