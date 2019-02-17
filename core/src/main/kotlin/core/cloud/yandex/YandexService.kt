package com.inspiritious.HoverTranslateWebExtension.core.cloud.yandex

import com.inspiritious.HoverTranslateWebExtension.core.cloud.yandex.dto.GetLangDto
import com.inspiritious.HoverTranslateWebExtension.core.utils.LanguageCode
import core.cloud.yandex.dto.TranslateDto
import core.cloud.yandex.model.LanguageChoices
import core.cloud.yandex.model.LanguagesResult
import core.cloud.yandex.model.TranslateResult
import kotlinx.coroutines.await
import kotlinx.serialization.DeserializationStrategy
import kotlin.browser.window
import kotlinx.serialization.json.JSON

object YandexService {

    private object YandexUrlProvider {

        private const val BaseUrl = "https://translate.yandex.net/api/v1.5/tr.json/"
        private const val ApiKey =
            "trnsl.1.1.20190208T220121Z.3ef5f299c652b692.097b99cbf97b783448762bf182b2957f683fa4fc"

        private fun Pair<LanguageCode, LanguageCode>.toLangDirection(): String =
            this.first.name.toLowerCase() + "-" + this.second.name.toLowerCase()


        fun getLangs() = "${BaseUrl}getLangs?key=$ApiKey"

        fun translate(text: String, fromLang: LanguageCode, toLang: LanguageCode) =
            "${BaseUrl}translate?key?${ApiKey}&text=$text&lang=${Pair(fromLang, toLang).toLangDirection()}"
    }

    private suspend fun<T> fetch(url : String, serializer: DeserializationStrategy<T>) : T {
        val text = window.fetch(url).await().text().await()
        return JSON.parse(serializer, text)
    }

    suspend fun translate(searchTerm : String, fromLang: LanguageCode, toLang: LanguageCode) : TranslateResult {

        return try {

            val result = fetch(
                YandexUrlProvider.translate(searchTerm, fromLang, toLang),
                TranslateDto.serializer())

            TranslateResult(true, searchTerm, result.text)

        } catch (e: dynamic) {
            console.log(e)
            TranslateResult(false, searchTerm, ArrayList())
        }
    }

    suspend fun getLanguages() : LanguagesResult {


        return try {

            val dto = fetch(YandexUrlProvider.getLangs(), GetLangDto.serializer())

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