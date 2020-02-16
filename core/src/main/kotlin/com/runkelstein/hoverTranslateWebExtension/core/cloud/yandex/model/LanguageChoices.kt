package com.runkelstein.hoverTranslateWebExtension.core.cloud.yandex.model

import com.runkelstein.hoverTranslateWebExtension.core.utils.LanguageCode

data class LanguageChoices(val source : LanguageCode, val targets : List<LanguageCode>)