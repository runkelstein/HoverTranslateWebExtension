package com.runkelstein.hoverTranslateWebExtension.core.storage

import com.runkelstein.hoverTranslateWebExtension.core.storage.YandexSettings
import kotlinx.serialization.Serializable

@Serializable
data class StorageProperties(
    var activatedKey: String  = "",
    var yandex : YandexSettings = YandexSettings()
)