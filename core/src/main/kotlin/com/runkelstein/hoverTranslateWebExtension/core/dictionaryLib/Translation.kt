package com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib

import com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib.DictOriginType
import kotlinx.serialization.Serializable

@Serializable
data class Translation(val sourceLangText : String, val targetLangText : String, val origin : DictOriginType)