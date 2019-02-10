package com.inspiritious.HoverTranslateWebExtension.core.cloud.yandex

import com.inspiritious.HoverTranslateWebExtension.core.cloud.yandex.dto.GetLangDto
import com.inspiritious.HoverTranslateWebExtension.core.utils.LanguageCode
import core.cloud.yandex.model.LanguageChoices
import core.cloud.yandex.model.LanguagesResult
import kotlinx.coroutines.await
import kotlin.browser.window
import kotlinx.serialization.json.JSON

object YandexService {

    private const val ApiKey = "trnsl.1.1.20190208T220121Z.3ef5f299c652b692.097b99cbf97b783448762bf182b2957f683fa4fc&"

    suspend fun getLanguages() : LanguagesResult {


        return try {

            val result =
                window
                    .fetch("https://translate.yandex.net/api/v1.5/tr.json/getLangs?key=$ApiKey")
                    .await().text().await()

            val dto = JSON.parse(GetLangDto.serializer(), result)

            val languageTargets = sequence {

                dto.dirs.forEach { textCodePair ->

                        val (source, target) = textCodePair
                            .split("-")
                            .map { LanguageCode.valueOf(it.toUpperCase()) }

                    yield(Pair(source, target))
                }
            }
            .groupBy ({ it.first }, { it.second })
            .map { LanguageChoices(it.key, it.value) }

            LanguagesResult(true, languageTargets)

        } catch (e: dynamic) {
            console.log(e)

            LanguagesResult(false, ArrayList())
        }


    }

}