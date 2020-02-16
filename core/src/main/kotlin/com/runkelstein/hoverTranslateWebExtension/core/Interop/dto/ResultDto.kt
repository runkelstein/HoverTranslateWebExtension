package com.runkelstein.hoverTranslateWebExtension.core.Interop.dto

import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable

@Serializable
class ResultDto<T>(val isSuccess : Boolean, val error : String, val payload : T)

typealias SimpleResultDto = ResultDto<String>
typealias DeferredSimpleResult = Deferred<ResultDto<String>>

fun resultWithError(error : String) = SimpleResultDto(false, error, "")
fun resultWithSucces() = SimpleResultDto(true, "", "")

fun <T>resultWithSuccess(payload: T) = ResultDto(true, "", payload)
fun <T>resultWithError(error : String, payload: T) = ResultDto(false, error, payload)
