package core.storage

import kotlinx.serialization.Serializable

@Serializable
data class StorageProperties(var activatedKey: String  = "")