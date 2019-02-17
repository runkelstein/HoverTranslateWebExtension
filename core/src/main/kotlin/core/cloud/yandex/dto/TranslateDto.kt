package core.cloud.yandex.dto

import kotlinx.serialization.Serializable

@Serializable
data class TranslateDto(val code : Int, val lang : String, val text : List<String>)