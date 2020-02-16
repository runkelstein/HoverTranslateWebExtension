package com.runkelstein.hoverTranslateWebExtension.core.storage

import com.runkelstein.hoverTranslateWebExtension.core.utils.LanguageCode
import kotlinx.serialization.Serializable

@Serializable
data class YandexSettings(var sourceLang : LanguageCode = LanguageCode.ES,
                          var targetLang : LanguageCode = LanguageCode.DE)