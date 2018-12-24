import core.dictionaryLib.DictCC
import core.storage.StorageEntry
import core.storage.StorageInfo
import core.storage.StorageMetadata
import core.storage.StorageService
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.table
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.browser.document
import kotlin.js.Date
import kotlin.math.pow

val addDictForm = document.getElementById("addDictForm") as HTMLFormElement
val addDictButton = document.getElementById("addDict") as HTMLInputElement
val clearDictButton = document.getElementById("clearDict") as HTMLInputElement
val fileInput = document.getElementById("fileInput") as HTMLInputElement
val dictionaryList = document.getElementById("dictionarList") as HTMLDivElement

var dictFile : File? = null

fun handleFileSelectionChanged(event : Event)
{
    val files = event.target?.asDynamic().files as FileList?;
    if (files == null) {
        console.log("not file list")
        return;
    }

    dictFile = files[0];
    addDictButton.disabled = false
}

fun handleAddDictionary(event : Event) {

    val file = dictFile ?: return

    addDictButton.disabled = true

    val reader = FileReader()
    reader.onload =::onFileLoaded
    reader.readAsText(file)

    dictFile = null
    addDictForm.reset()
}

fun onFileLoaded(event :Event) {

    val content = event.target.asDynamic().result
    val dict = DictCC(content)

    StorageService.saveEntry(StorageEntry(StorageInfo(dict.description, content.length, Date()), content))
        .then { refreshDictionaryTable() }
}

fun main(args: Array<String>) {
    console.log("options loaded")
    fileInput.onchange = ::handleFileSelectionChanged
    addDictButton.onclick = ::handleAddDictionary
    clearDictButton.onclick = ::handleClearDictionaries
    refreshDictionaryTable()
}

fun handleClearDictionaries(event: Event) {
    StorageService.clear().then { refreshDictionaryTable()  }
}

fun refreshDictionaryTable() {

    StorageService.loadMetadata().then {
        initDictionaryTable(it)
    }

}

fun removeDictionary(info : StorageInfo) {
    StorageService.remove(info).then {
        refreshDictionaryTable()
    }
}

fun activateDictionary(metadata: StorageMetadata, info: StorageInfo) {

    metadata.activated = info
    StorageService.saveProperties(metadata.properties)

}

fun Int.toFormatedMegaByte() = (this / 2f.pow(20)).asDynamic().toFixed(2).toString() + " MB"

fun initDictionaryTable(metadata : StorageMetadata)
{
    dictionaryList.innerHTML = ""
    dictionaryList.appendChild(
        document.create.table(classes = "dict") {

            thead {
                tr {
                    th { +"Description" }
                    th(classes="short") { +"File Size"}
                    th{ +"Last Updated"}
                    th(classes="short") { +"Activated"}
                    th(classes="remove")
                }
            }
            tbody {

                for (info in metadata.infoList) {
                    tr {
                        td { +info.key }
                        td { +info.size.toFormatedMegaByte() }
                        td { + info.updated.toLocaleString()}
                        td {
                            radioInput {
                                id = "select_${info.key}"
                                name = "select_activation"
                                value = info.key
                                checked = metadata.isActivated(info)
                                onClickFunction = { activateDictionary(metadata, info) }
                            }
                        }
                        td(classes="remove"){
                            buttonInput {
                                id = "remove_${info.key}"
                                value = "Remove"
                                onClickFunction = { removeDictionary(info) }
                            }
                        }

                    }
                }

            }
        }
    )
}


