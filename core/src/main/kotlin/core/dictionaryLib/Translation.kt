package com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib

import core.dictionaryLib.DictOriginType
import kotlinx.serialization.Serializable

@Serializable
data class Translation(val sourceLangText : String, val targetLangText : String, val origin : DictOriginType)