package com.inspiritious.HoverTranslateWebExtension.core.storage

import kotlinx.serialization.Serializable

@Serializable
data class StorageEntry( val info : StorageInfo, val  content: String)