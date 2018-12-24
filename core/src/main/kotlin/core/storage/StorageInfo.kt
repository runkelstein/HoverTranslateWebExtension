package core.storage

import kotlin.js.Date
import kotlinx.serialization.Serializable

@Serializable
data class StorageInfo(val key : String, val size : Int,
                       @Serializable(with=DateSerializer::class)
                            val updated : Date)