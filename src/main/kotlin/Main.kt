import browserAction.Details3
import extensionTypes.InjectDetails
import notifications.CreateNotificationOptions
import tabs.Tab
import webextensions.browser
import kotlin.js.Promise

var isActive = false;
const val CONTENT_SCRIPT_PATH = "content_script/build/kotlin-js-min/main"

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

        browser.runtime.openOptionsPage()

        if (!isActive) {
            injectContentScript().then { sendCommandToTab(tab,"EnableTranslationPlugin") }
        } else {
            sendCommandToTab(tab, "DisableTranslationPlugin")
        }
    }

    browser.runtime.onMessage.addListener { message, _, _ ->
        if (message.command === "TranslationPluginEnabled") {
            enablePlugin()
        } else if (message.command === "TranslationPluginDisabled") {
            disablePlugin()
        }
    }

    browser.tabs.onActivated.addListener { disablePlugin() }
    browser.tabs.onUpdated.addListener { _,_,_ -> disablePlugin() }
}

fun main(args: Array<String>) {

    listenForActions()
}

fun injectContentScript(): Promise<Array<out Array<dynamic>?>> {
    return Promise.all(
        arrayOf(
            injectContentScriptFile("kotlin.js"),
            injectContentScriptFile("declarations.js"),
            injectContentScriptFile("content_script.js"))
    )
}

fun sendCommandToTab(tab: Tab, cmd : String){
    browser.tabs.sendMessage(tab.id!!, jsObject { command = cmd })
}

fun sendBrowserNotification(message : String) {
    browser.notifications.create(options = CreateNotificationOptions(
        type = "basic",
        title = "Hover Translation Plugin",
        message = message)
    )
}

inline fun injectContentScriptFile(fileName : String) = browser.tabs.executeScript(details = InjectDetails(file = "$CONTENT_SCRIPT_PATH/$fileName"))

inline fun jsObject(init: dynamic.() -> Unit): dynamic {
    val o = js("{}")
    init(o)
    return o
}