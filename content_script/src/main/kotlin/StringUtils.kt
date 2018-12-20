
// here we put all general purpose extension functions concerning Strings import kotlin.text.*

private val wordDelimiters = charArrayOf(' ', ',', '.', ';', '?', '‘', '’', '\'', '"', '`', '´', '(', ')')

fun String.extractWord(cursorPos : Int) : String  {
    val startPos = lastIndexOfAny(wordDelimiters,cursorPos)+1
    var endPos = indexOfAny(wordDelimiters,cursorPos)
    endPos = if (endPos==-1) this.length else endPos
    return substring(startPos, endPos)
}

fun String.isWord() =  this.isNotEmpty() && wordDelimiters.all { !this.contains(it) }

