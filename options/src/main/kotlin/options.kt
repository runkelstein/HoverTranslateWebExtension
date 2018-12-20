import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.browser.document

val settingsForm = document.getElementById("settingsForm") as HTMLFormElement
val addDictButton = document.getElementById("addDict") as HTMLInputElement
val fileInput = document.getElementById("fileInput") as HTMLInputElement

var dictFile : File? = null

fun handleFileSelectionChanged(event : Event)
{
    var files = event.target?.asDynamic().files as FileList?;
    if (files == null) {
        console.log("not file list")
        return;
    }

    dictFile = files[0];
    addDictButton.disabled = false
}

fun handleAddDictionary(event : Event) {

    var file = dictFile
    if (file == null) {
        return
    }

    addDictButton.disabled = true

    var reader = FileReader();

    reader.onload ={
        console.log(it.target.asDynamic().result)
    }

    reader.readAsText(file)
    dictFile = null
    settingsForm.reset()
}

fun main(args: Array<String>) {

    console.log("options loaded")
    fileInput.onchange = ::handleFileSelectionChanged
    addDictButton.onclick = ::handleAddDictionary
}


