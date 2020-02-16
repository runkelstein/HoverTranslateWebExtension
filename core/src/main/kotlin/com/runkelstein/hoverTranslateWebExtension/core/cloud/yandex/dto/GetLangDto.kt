package com.runkelstein.hoverTranslateWebExtension.core.cloud.yandex.dto

import kotlinx.serialization.Serializable

@Serializable
data class GetLangDto(val dirs: List<String>)