package com.runkelstein.hoverTranslateWebExtension.core.cloud.yandex.model

class TranslateResult(isSuccess : Boolean, val searchTerm : String, val results : List<String>) : ServiceResult(isSuccess)