package core.storage

import com.inspiritious.HoverTranslateWebExtension.core.utils.LanguageCode
import kotlinx.serialization.Serializable

@Serializable
data class YandexSettings(var sourceLang : LanguageCode = LanguageCode.ES,
                          var targetLang : LanguageCode = LanguageCode.DE)