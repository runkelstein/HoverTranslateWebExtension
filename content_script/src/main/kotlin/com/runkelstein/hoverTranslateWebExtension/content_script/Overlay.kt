package com.runkelstein.hoverTranslateWebExtension.content_script

import com.runkelstein.hoverTranslateWebExtension.core.Interop.api.ContentMessageService
import com.runkelstein.hoverTranslateWebExtension.core.Interop.api.send
import com.runkelstein.hoverTranslateWebExtension.core.Interop.commands.RequestTranslationCommand
import com.runkelstein.hoverTranslateWebExtension.core.Interop.dto.ResultDto
import com.runkelstein.hoverTranslateWebExtension.core.dictionaryLib.SearchResult
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.*

import kotlin.browser.document
import kotlin.dom.addClass

class Overlay {

    companion object {
        const val MOUSE_POINTER_MARGIN = 15
        const val HEIGHT = 300;
        const val WIDTH = 300;
    }

    val mainElement = document.createElement("DIALOG") as HTMLElement

    var lastWord = ""
    var currentWord = ""

    var isVisible : Boolean = false
        set(value) {
            if (value) {
                mainElement.setAttribute("open","")
            } else {
                mainElement.removeAttribute("open")
            }
        }

    init {

        mainElement.addClass("pure")
        with(mainElement.style) {
            background = "white"
            position = "absolute"
            border = "0"
            padding = "0"
            zIndex = "99999"
            setProperty("border-radius","0.6rem")
            setProperty("border-shadow","0 0 1em black")
            //width = pixel(WIDTH)
        }


    }

    fun enable() {
        document.body!!.appendChild(mainElement)
        document.addEventListener("mousemove", ::onMouseMove)
    }

    private fun updateOverlayContent(searchResult : SearchResult) {

        if (searchResult.results.isEmpty()) {
            mainElement.innerHTML = """
                <div style="background:darkred; padding:10px">
                    <span style="color: white; font-weight:bold;">
                    For the word <span style="color: yellow">${searchResult.searchTerm}</span> no translation is available
                    </span>
                </div>""".trimIndent()
            return
        }

        var builder = StringBuilder();
        builder.appendHTML().table(classes = "pure-table pure-table-striped pure-u-1-1") {
                thead {
                    tr {
                        th { +"Source" }
                        th { +"Target" }
                    }
                }

                tbody {


                    for (translation in searchResult.results.take(8)) {
                        tr {
                            td {
                                +translation.sourceLangText
                            }
                            td {
                                +translation.targetLangText
                            }
                        }
                    }
                }
            }

        mainElement.innerHTML = builder.toString()
    }

    fun disable() {
        document.removeEventListener("mousemove", ::onMouseMove)
        document.body!!.removeChild(mainElement)
    }

    private fun onMouseMove(event: Event)  {
        if (event !is MouseEvent) {
            return
        }

        mainElement.style.left = pixel(event.pageX+MOUSE_POINTER_MARGIN)
        mainElement.style.top = pixel(event.pageY+MOUSE_POINTER_MARGIN)

        var caretPosition = document.caretPositionFromPoint(event.pageX, event.pageY)

        lastWord = currentWord
        currentWord = extractWord(caretPosition, event)

        isVisible = lastWord.isWord()

        if (lastWord != currentWord) {

            lastWord = currentWord;
            mainElement.innerHTML = ""

            GlobalScope.launch {

                val capturedWord = currentWord

                delay(250) // throttle update
                if (capturedWord != lastWord ) {
                    return@launch
                }

                val result : Deferred<ResultDto<SearchResult>> = ContentMessageService.send(RequestTranslationCommand(currentWord))

                if (result.await().isSuccess) {
                    var payload = result.await().payload;
                    updateOverlayContent(payload)
                } else {
                    console.log(result.await().error)
                }

            }
        }
    }

    private fun extractWord(caretPosition: CaretPosition?, event : MouseEvent) : String {

        if (caretPosition==null
            || !caretPosition.isTextNode()
            || !caretPosition.getClientRect().isWithinBoundaries(event.pageX, event.pageY)) {
            return ""
        }

        val content = caretPosition.offsetNode.textContent ?: ""
        if (content.isEmpty()) {
            return ""
        }

        return content.extractWord(caretPosition.offset);
    }


    private fun pixel(value : Int) = "${value}px"
    private fun pixel(value : Double) = "${value}px"

}