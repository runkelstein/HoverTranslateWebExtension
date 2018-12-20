import org.w3c.dom.CaretPosition
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

import kotlin.browser.document

class Overlay {

    companion object {
        const val MOUSE_POINTER_MARGIN = 15
        const val HEIGHT = 200;
        const val WIDTH = 200;
    }

    val mainElement = document.createElement("div") as HTMLDivElement
    val mainParagraph = document.createElement("p") as HTMLParagraphElement

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
            height =  pixel(HEIGHT)
            width = pixel(WIDTH)
        }

        mainElement.appendChild(mainParagraph)
    }

    fun enable() {
        document.body!!.appendChild(mainElement)
        document.addEventListener("mousemove", ::onMouseMove)
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
        console.log("event.pageX: ${event.pageX}; event.pageY: ${event.pageY}")
        console.log("event.clientX: ${event.clientX}; event.clientY: ${event.clientY}")
        console.log("event.screenX: ${event.screenX}; event.screenY: ${event.screenY}")
        currentWord = extractWord(caretPosition, event)

        isVisible = currentWord.isWord()
        mainParagraph.innerHTML = "<b>$currentWord</b>"
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