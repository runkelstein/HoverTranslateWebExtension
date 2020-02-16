package com.runkelstein.hoverTranslateWebExtension.core.storage

data class StorageMetadata(
    val properties : StorageProperties,
    val infoList : List<StorageInfo>)
{
    var activated : StorageInfo?
            get() = infoList.find(::isActivated)
            set(value) { properties.activatedKey = value?.key.orEmpty() }

    fun isActivated(info : StorageInfo) = properties.activatedKey == info.key
}