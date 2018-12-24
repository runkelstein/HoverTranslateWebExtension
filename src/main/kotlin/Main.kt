import browserAction.Details3
import core.dictionaryLib.DictCC
import core.dictionaryLib.Dictionary
import core.dictionaryLib.SearchResult
import core.storage.StorageService
import extensionTypes.InjectDetails
import notifications.CreateNotificationOptions
import tabs.Tab
import webextensions.browser
import kotlin.js.Promise
import core.utils.jsObject

import kotlinx.serialization.json.JSON

var isActive = false;

const val CONTENT_SCRIPT_PATH = "content_script/build/kotlin-js-min/main"

var dictionary: Dictionary? = null

fun disablePlugin() {
    isActive = false
    selectToolbarIcon()
}

fun enablePlugin() {
    isActive = true
    selectToolbarIcon()
}

fun selectToolbarIcon() {
    if (isActive) {
        browser.browserAction.setIcon(Details3(path = "icons/hoverTranslate128_active.png"))
    } else {
        browser.browserAction.setIcon(Details3(path = "icons/hoverTranslate128.png"))
    }
}

fun listenForActions() {
    browser.browserAction.onClicked.addListener {tab ->

        if (!isActive) {
            injectContentScript().then { sendMessageToTab(tab,"EnableTranslationPlugin") }
        } else {
            sendMessageToTab(tab, "DisableTranslationPlugin")
        }
    }

    browser.runtime.onMessage.addListener { message, sender, _ ->
        if (message.command === "TranslationPluginEnabled") {
            enablePlugin()
        } else if (message.command === "TranslationPluginDisabled") {
            disablePlugin()
        }

        // todo: refactor towards command based approach
        if (message.command == "Translate" && dictionary!=null && sender.tab!=null) {
            val searchResult = dictionary!!.findTranslations(message.data)
            sendMessageToTab(sender.tab!!, "Translated", JSON.stringify(SearchResult.serializer(), searchResult))
        }
    }

    browser.tabs.onActivated.addListener { disablePlugin() }
    browser.tabs.onUpdated.addListener { _,_,_ -> disablePlugin() }
}

fun main(args: Array<String>) {

    // todo: maybee this will look much nicer with coroutines
    StorageService.loadMetadata().then {metaData ->
        if (metaData.activated == null) {
            return@then
        }

        StorageService.load(metaData.activated!!)
            .then {

                if (it!=null) {
                    dictionary = DictCC(it.content)
                    val testWord = dictionary!!.findTranslations("vacanza")
                    sendBrowserNotification("Dictionary initialized: " + testWord.results.first().targetLangText)
                } else {
                    sendBrowserNotification("Dictionary not initialized: ")
                }

            }
    }
    listenForActions()
}

fun injectContentScript(): Promise<Array<out Array<dynamic>?>> {
    return Promise.all(
        arrayOf(
            injectContentScriptFile("kotlin.js"),
            injectContentScriptFile("declarations.js"),
            injectContentScriptFile("kotlinx-html-js.js"),
            injectContentScriptFile("kotlinx-serialization-runtime-js.js"),
            injectContentScriptFile("core.js"),
            injectContentScriptFile("content_script.js"))
    )
}

fun sendMessageToTab(tab: Tab, cmd : String){
    browser.tabs.sendMessage(tab.id!!, jsObject { command = cmd })
}

fun sendMessageToTab(tab: Tab, cmd : String, anyData : Any){
    browser.tabs.sendMessage(tab.id!!, jsObject { command = cmd; data = anyData })
}

fun sendBrowserNotification(message : String) {
    browser.notifications.create(options = CreateNotificationOptions(
        type = "basic",
        title = "Hover Translation Plugin",
        message = message)
    )
}

fun injectContentScriptFile(fileName : String) = browser.tabs.executeScript(details = InjectDetails(file = "$CONTENT_SCRIPT_PATH/$fileName"))