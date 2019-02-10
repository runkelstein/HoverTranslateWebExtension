import com.inspiritious.HoverTranslateWebExtension.core.cloud.yandex.YandexService
import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.DictCC
import com.inspiritious.HoverTranslateWebExtension.core.storage.StorageEntry
import com.inspiritious.HoverTranslateWebExtension.core.storage.StorageInfo
import com.inspiritious.HoverTranslateWebExtension.core.storage.StorageMetadata
import com.inspiritious.HoverTranslateWebExtension.core.storage.StorageService
import core.cloud.yandex.model.LanguageChoices
import core.storage.YandexSettings
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
val yandexChoice = document.getElementById("yandexChoice") as HTMLDivElement

var dictFile : File? = null

fun handleFileSelectionChanged(event : Event)
{
    val files = event.target?.asDynamic().files as FileList?;
    if (files == null) {
        console.log("not file list")
        return
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

fun onFileLoaded(event :Event) = GlobalScope.launch {

    val content = event.target.asDynamic().result
    val dict = DictCC(content)

    StorageService.saveEntry(StorageEntry(StorageInfo(dict.description, content.length, Date()), content))
    refreshDictionaryTable()
}

 fun main(args: Array<String>) {
    console.log("options loaded")

    fileInput.onchange = ::handleFileSelectionChanged
    addDictButton.onclick = ::handleAddDictionary
    clearDictButton.onclick = ::handleClearDictionaries

    GlobalScope.launch {
        refreshDictionaryTable()
        refreshYandexChoiceForm()
    }
}


fun handleClearDictionaries(event: Event) = GlobalScope.launch {
    StorageService.clear()
    refreshDictionaryTable()
}

suspend fun refreshDictionaryTable() {
    val metadata = StorageService.loadMetadata()
    initDictionaryTable(metadata)
}

suspend fun refreshYandexChoiceForm() {

    val yandexResult = YandexService.getLanguages()
    val metadata = StorageService.loadMetadata()

    if (yandexResult.isSuccess) {
        initYandexChoiceForm(yandexResult.languages, metadata.properties.yandex)
    }


}

fun removeDictionary(info : StorageInfo) = GlobalScope.launch {
    StorageService.remove(info)
    refreshDictionaryTable()
}

fun activateDictionary(info: StorageInfo) = GlobalScope.launch {

    val metadata = StorageService.loadMetadata()
    metadata.activated = info
    StorageService.saveProperties(metadata.properties)
    initDictionaryTable(metadata)

}

fun updateYandexSettings(settings : YandexSettings) = GlobalScope.launch {

    val metadata = StorageService.loadMetadata()
    console.log(settings)
    metadata.properties.yandex = settings
    console.log(settings)
    StorageService.saveProperties(metadata.properties)


}

fun Int.toFormatedMegaByte() = (this / 2f.pow(20)).asDynamic().toFixed(2).toString() + " MB"

fun initDictionaryTable(metadata : StorageMetadata)
{
    dictionaryList.innerHTML = ""
    dictionaryList.appendChild(
        document.create.table(classes = "pure-table pure-table-striped pure-u-1-1") {

            thead {
                tr {
                    th{ +"Description" }
                    th{ +"File Size"}
                    th{ +"Last Updated"}
                    th{ +"Activated"}
                    th()
                }
            }
            tbody {

                for (info in metadata.infoList) {

                    var isActivated = metadata.isActivated(info);

                    tr {
                        td { +info.key }
                        td { +info.size.toFormatedMegaByte() }
                        td { +info.updated.toLocaleString()}
                        td {
                            label {
                                radioInput {

                                    id = "select_${info.key}"
                                    name = "select_activation"
                                    value = info.key
                                    checked = isActivated
                                    onClickFunction = { activateDictionary(info) }
                                }
                                span () {
                                    if (isActivated) +" active" else + " inactive"
                                }
                            }

                        }
                        td{
                            buttonInput(classes="pure-button  pure-button-primary") {
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

fun initYandexChoiceForm(
    languages : List<LanguageChoices>, settings : YandexSettings) {
    yandexChoice.innerHTML = ""

    val langSourceFieldName = "yandexSourceLang"
    val langTargetFieldName = "yandexTargetLang"


    val targetChoices = languages.find { it.source == settings.sourceLang }?.targets ?: ArrayList()

    yandexChoice.appendChild(document.create.div(classes = "pure-g") {
        style = "width: 600px; line-height:40px"
        div(classes = "pure-u-1-8") {
            style="vertical-align: middle; text-align:right"
            label {
                htmlFor = langSourceFieldName
                style="margin-right: 10px"
                +"source" }
        }
        div(classes = "pure-u-1-4") {
            select {
                name = langSourceFieldName

                for (lang in languages) {

                    option {
                        value = lang.source.toString()
                        selected = settings.sourceLang == lang.source


                        +lang.source.description

                        onClickFunction = {
                            settings.sourceLang = lang.source
                            initYandexChoiceForm(languages, settings)
                        }
                    }

                }

            }
        }
        div(classes = "pure-u-1-8") {
            style="vertical-align: middle; text-align:right"
            label {
                htmlFor = langTargetFieldName;
                style="margin-right: 10px"
                +"target" }
        }
        div(classes = "pure-u-1-4") {
            select {
                name = langTargetFieldName

                for (lang in targetChoices) {

                    option {
                        value = lang.toString()
                        selected = settings.targetLang == lang

                        +lang.description

                        onClickFunction = {
                            settings.targetLang = lang
                            initYandexChoiceForm(languages, settings)
                        }
                    }

                }

            }

        }
        div(classes = "pure-u-1-4") {
            buttonInput(classes = "pure-button pure-button-primary") {
                id = "submitYandexSettings"
                value = "Submit"
                onClickFunction = { updateYandexSettings(settings); }
            }
        }

    })

}


