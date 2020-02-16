package com.runkelstein.hoverTranslateWebExtension.core.storage

import kotlinx.serialization.*
import kotlinx.serialization.json.JSON
import com.runkelstein.hoverTranslateWebExtension.core.utils.jsObject

import kotlinx.coroutines.await
import kotlinx.serialization.protobuf.ProtoBuf
import webextensions.browser

object StorageService {

    private const val INFO_KEY = "StorageInfos";
    private const val META_DATA_KEY ="StorageMetaData"

    private inline fun List<StorageInfo>.toMap() = this.associateBy(StorageInfo::key).toMutableMap()
    private inline fun Map<String, StorageInfo>.toJSON() = JSON.stringify(StorageInfo.serializer().list,this.values.toList())
    private inline fun StorageProperties.toJSON() = JSON.stringify(StorageProperties.serializer(), this)
    private inline fun StorageEntry.toBinaryHexString() = ProtoBuf.dumps(StorageEntry.serializer(), this)

    suspend fun saveProperties(properties: StorageProperties)
    {
        val pair = jsObject()
        pair[META_DATA_KEY] = properties.toJSON()
        browser.storage.sync.set(pair).await()
    }

    suspend fun saveEntry(entry: StorageEntry) {

        val pair = jsObject()
        val infoMap = loadInfos().toMap()

        infoMap[entry.info.key] = entry.info
        pair[INFO_KEY] = infoMap.toJSON()
        pair[entry.info.key] = entry.toBinaryHexString()

        browser.storage.sync.set(pair).await()
    }

    suspend fun loadMetadata() : StorageMetadata {

        val properties = loadProperties()
        val infos = loadInfos()

        return StorageMetadata(properties, infos)
    }

    suspend fun load(info: StorageInfo) : StorageEntry? {

        val results = browser.storage.sync.get(info.key).await()
        return try {
            //todo: once https://youtrack.jetbrains.com/issue/KT-29003 gets fixed we can return to JSON
            ProtoBuf.loads(StorageEntry.serializer(), results[info.key])
        } catch (e: dynamic) {
            console.log(e)
            null
        }
    }

    private suspend fun loadProperties() : StorageProperties {

        val result = browser.storage.sync.get(META_DATA_KEY).await()

        return try {
            JSON.parse(StorageProperties.serializer(), result[META_DATA_KEY])
        } catch(e:dynamic) {
            console.log(e)
            StorageProperties()
        }
    }

    private suspend fun loadInfos(): List<StorageInfo> {

        val resultsPromise = browser.storage.sync.get(INFO_KEY)
        val items = resultsPromise.await()

        return try {
            JSON.parse(StorageInfo.serializer().list, items[INFO_KEY])
        } catch(e:dynamic) {
            console.log(e)
            ArrayList()
        }

    }

    suspend fun remove(info : StorageInfo)
    {
        val pair = jsObject()

        val infoMap = loadInfos().toMap()
        infoMap.remove(info.key)
        pair[INFO_KEY] = infoMap.toJSON()

        browser.storage.sync.set(pair).await()
        browser.storage.sync.remove(info.key).await()
    }

    suspend fun clear() = browser.storage.sync.clear().await()

}