import core.dictionaryLib.SearchResult
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.table
import kotlinx.html.stream.appendHTML
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import runtime.MessageSender
import webextensions.browser
import kotlinx.serialization.json.JSON
import org.w3c.dom.*

import kotlin.browser.document

class Overlay {

    companion object {
        const val MOUSE_POINTER_MARGIN = 15
        const val HEIGHT = 300;
        const val WIDTH = 300;
    }

    val mainElement = document.createElement("div") as HTMLDivElement
    val mainParagraph = document.createElement("p") as HTMLParagraphElement

    var lastWord = ""
    var currentWord = ""

    var isVisible : Boolean = false
        set(value) {
            mainElement.style.visibility = if (value) "visible" else "hidden"
        }

    init {
        with(mainElement.style) {
            background = "white"
            borderColor = "black"
            borderWidth = pixel(1)
            borderStyle = "solid"
            position = "absolute"
            width = pixel(WIDTH)
        }

        mainElement.appendChild(mainParagraph)
    }

    fun enable() {
        document.body!!.appendChild(mainElement)
        document.addEventListener("mousemove", ::onMouseMove)


        browser.runtime.onMessage.addListener(::OnMessageReceived);
    }

    private fun OnMessageReceived(message: dynamic, messageSender: MessageSender, function: () -> Unit) {
        if (message.command != "Translated") {
            return
        }

        var result = message.data as String?;
        if (result==null) {
            return
        }

        var searchResult = JSON.parse(SearchResult.serializer(), result)
        
        var builder = StringBuilder();
        builder.appendHTML().table {
                thead {
                    tr {
                        th { +"Source" }
                        th { +"Target" }
                    }
                }

                tbody {
                    for (translation in searchResult.results) {
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
        browser.runtime.onMessage.removeListener(::OnMessageReceived)
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

        isVisible = currentWord.isWord()

        if (lastWord != currentWord) {
            sendCommandToBackgroundScript("Translate", currentWord)
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