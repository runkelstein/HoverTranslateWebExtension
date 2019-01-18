package com.inspiritious.HoverTranslateWebExtension.core.storage

import kotlinx.serialization.*
import kotlinx.serialization.json.JSON
import com.inspiritious.HoverTranslateWebExtension.core.utils.jsObject
import kotlinx.serialization.protobuf.ProtoBuf
import webextensions.browser
import kotlin.js.Promise

object StorageService {

    const val INFO_KEY = "StorageInfos";
    const val META_DATA_KEY ="StorageMetaData"

    private inline fun List<StorageInfo>.toMap() = this.associateBy(StorageInfo::key).toMutableMap()
    private inline fun Map<String, StorageInfo>.toJSON() = JSON.stringify(StorageInfo.serializer().list,this.values.toList())
    private inline fun StorageProperties.toJSON() = JSON.stringify(StorageProperties.serializer(), this)
    private inline fun StorageEntry.toBinaryHexString() = ProtoBuf.dumps(StorageEntry.serializer(), this)

    fun saveProperties(properties: StorageProperties) : Promise<*>
    {
        val pair = jsObject()
        pair[META_DATA_KEY] = properties.toJSON()
        return browser.storage.sync.set(pair)
    }

    fun saveEntry(entry: StorageEntry): Promise<*> {

        val pair = jsObject()

        return loadInfos().then {
            val infoMap = it.toMap()
            infoMap[entry.info.key] = entry.info
            pair[INFO_KEY] = infoMap.toJSON()
            pair[entry.info.key] = entry.toBinaryHexString()
        }.then {
            browser.storage.sync.set(pair)
        }
    }

    fun loadMetadata() : Promise<StorageMetadata> {

        lateinit var infos : List<StorageInfo>
        lateinit var properties : StorageProperties

        loadProperties()

        return Promise.all(arrayOf(
            loadProperties().then { properties = it },
            loadInfos().then { infos = it  }))
            .then { StorageMetadata(properties, infos) }
    }

    fun load(info: StorageInfo) : Promise<StorageEntry?>  {
        val resultsPromise = browser.storage.sync.get(info.key)
        return resultsPromise.then {
            try {
                //todo: once https://youtrack.jetbrains.com/issue/KT-29003 gets fixed
                //we can return to JSON
                ProtoBuf.loads(StorageEntry.serializer(), it[info.key])
            }
            catch(e:dynamic) {
                console.log(e)
                null
            }
        }
    }

    private fun loadProperties() : Promise<StorageProperties> {
        val resultsPromise = browser.storage.sync.get(META_DATA_KEY)
        return resultsPromise.then {
            try {
                JSON.parse(StorageProperties.serializer(), it[META_DATA_KEY])
            }
            catch(e:dynamic) {
                console.log(e)
                StorageProperties()
            }
        }
    }

    private fun loadInfos(): Promise<List<StorageInfo>> {
        val resultsPromise = browser.storage.sync.get(INFO_KEY)
        return resultsPromise.then {
            try {
                JSON.parse(StorageInfo.serializer().list, it[INFO_KEY]) }
            catch(e:dynamic) {
                console.log(e)
                ArrayList<StorageInfo>()
            }
        }
    }

    fun remove(info : StorageInfo) : Promise<*>
    {
        val pair = jsObject()

        return loadInfos().then {
            val infoMap = it.toMap()
            infoMap.remove(info.key)
            pair[INFO_KEY] = infoMap.toJSON()
        }.then {
            browser.storage.sync.set(pair)
            browser.storage.sync.remove(info.key)
        }
    }

    fun clear(): Promise<*> {
        return browser.storage.sync.clear()
    }

}