package core.Interop.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResultDto(val isSuccess : Boolean, val error : String)