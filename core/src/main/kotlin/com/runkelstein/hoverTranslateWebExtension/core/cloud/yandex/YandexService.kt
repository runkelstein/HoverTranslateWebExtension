package com.runkelstein.hoverTranslateWebExtension.core.cloud.yandex

import com.runkelstein.hoverTranslateWebExtension.core.cloud.yandex.dto.GetLangDto
import com.runkelstein.hoverTranslateWebExtension.core.utils.LanguageCode
import com.runkelstein.hoverTranslateWebExtension.core.cloud.yandex.dto.TranslateDto
import com.runkelstein.hoverTranslateWebExtension.core.cloud.yandex.model.LanguageChoices
import com.runkelstein.hoverTranslateWebExtension.core.cloud.yandex.model.LanguagesResult
import com.runkelstein.hoverTranslateWebExtension.core.cloud.yandex.model.TranslateResult
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

        private fun operation(operation: String) = BaseUrl + operation + "?key=${ApiKey}"

        private fun param(param : String, value : String) = "&${param}=${value}"

        fun getLangs() = operation("getLangs")

        fun translate(text: String, fromLang: LanguageCode, toLang: LanguageCode) =
            operation("translate") +
                    param("text", text) +
                    param("lang", Pair(fromLang, toLang).toLangDirection())


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

            TranslateResult(
                true,
                searchTerm,
                result.text
            )

        } catch (e: dynamic) {
            console.log(e)
            TranslateResult(
                false,
                searchTerm,
                ArrayList()
            )
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
            .map {
                LanguageChoices(
                    it.key,
                    it.value
                )
            }

            LanguagesResult(true, languageTargets)

        } catch (e: dynamic) {
            console.log(e)

            LanguagesResult(false, ArrayList())
        }


    }

}