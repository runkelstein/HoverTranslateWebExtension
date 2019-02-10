package com.inspiritious.HoverTranslateWebExtension.core.storage

import com.inspiritious.HoverTranslateWebExtension.core.utils.LanguageCode
import core.storage.YandexSettings
import kotlinx.serialization.Serializable

@Serializable
data class StorageProperties(
    var activatedKey: String  = "",
    var yandex : YandexSettings = YandexSettings())