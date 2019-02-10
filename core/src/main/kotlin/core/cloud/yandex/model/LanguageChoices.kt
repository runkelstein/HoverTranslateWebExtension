package core.cloud.yandex.model

import com.inspiritious.HoverTranslateWebExtension.core.utils.LanguageCode

data class LanguageChoices(val source : LanguageCode, val targets : List<LanguageCode>)