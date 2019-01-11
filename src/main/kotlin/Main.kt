import browserAction.Details3
import core.Interop.api.BackgroundMessageService
import core.Interop.api.IMessageService
import core.Interop.api.send
import core.Interop.commands.RequestTranslationCommand
import core.Interop.commands.SwitchPluginCommand
import core.Interop.dto.DeferredSimpleResult
import core.Interop.dto.ResultDto
import core.Interop.dto.resultWithSuccess
import core.dictionaryLib.DictCC
import core.dictionaryLib.IDictionary
import core.dictionaryLib.SearchResult
import core.storage.StorageService
import core.utils.await
import extensionTypes.InjectDetails
import notifications.CreateNotificationOptions
import tabs.Tab
import webextensions.browser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tabs.ActiveInfo
import tabs.ChangeInfo
import core.Interop.api.onReceive

var isActive = false;

const val CONTENT_SCRIPT_PATH = "content_script/build/kotlin-js-min/main"
const val CSS_PATH = "options"

val messageService : IMessageService = BackgroundMessageService

val contentInjectedTabs = HashSet<Int>();
val pluginEnabledTabs = HashSet<Int>();
lateinit var dictionary: IDictionary

fun deactivateToolbar() {
    isActive = false
    selectToolbarIcon()
}

fun enablePlugin(tabId : Int, isEnabled : Boolean) {
    if (isEnabled) {
        pluginEnabledTabs.add(tabId)
        activateToolbar()
    } else {
        deactivateToolbar()
        pluginEnabledTabs.remove(tabId)
    }
}

fun isPluginEnabled(tabId : Int) = pluginEnabledTabs.contains(tabId)

fun activateToolbar() {
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


fun onToolbarButtonClicked(tab : Tab) {
    val tabId = tab.id ?: return

    GlobalScope.launch {

        injectContentScript(tab)

        val isEnabled = !isPluginEnabled(tabId)
        val result : DeferredSimpleResult = messageService.send(SwitchPluginCommand(tabId, isEnabled))

        if (result.await().isSuccess) {
            enablePlugin(tabId, isEnabled)
        } else {
            sendBrowserNotification(result.await().error)
        }

    }
}

fun onTabActivated(info : ActiveInfo) {
    if (isPluginEnabled(info.tabId)) {
        activateToolbar()
    } else {
        deactivateToolbar()
    }
}

fun onTabUpdated(tabId : Int, changeInfo : ChangeInfo, tab : Tab) {

    if (changeInfo.status != "loading") {
        return
    }

    console.log("Tab $tabId is reloading.")
    contentInjectedTabs.remove(tabId);
    enablePlugin(tabId, false)
}

fun addListeners() {
    browser.browserAction.onClicked.addListener (::onToolbarButtonClicked)
    browser.tabs.onActivated.addListener (::onTabActivated)
    browser.tabs.onUpdated.addListener (::onTabUpdated)
    BackgroundMessageService.onReceive(::onTranslationRequest)
}

fun onTranslationRequest(cmd : RequestTranslationCommand) : ResultDto<SearchResult> {
    console.log("translation request received")
    val searchResult = dictionary.findTranslations(cmd.searchTerm)
    return resultWithSuccess(searchResult)
}

fun loadDictionary() {

    StorageService.loadMetadata().then {metaData ->
        if (metaData.activated == null) {
            return@then
        }

        StorageService.load(metaData.activated!!)
            .then {

                if (it!=null) {
                    dictionary = DictCC(it.content)
                    val testWord = dictionary.findTranslations("vacanza")
                    sendBrowserNotification("Dictionary initialized: " + testWord.results.first().targetLangText)
                } else {
                    sendBrowserNotification("Dictionary not initialized: ")
                }

            }
    }
}

fun main(args: Array<String>) {
    loadDictionary()
    addListeners()
}

suspend fun injectContentScript(tab :Tab) {

    if (!contentInjectedTabs.add(tab.id!!)) {
        console.log("content scripts already injected into tab ${tab.id}")
        return;
    }

    console.log("inject content scripts into ${tab.id}")

    injectCss(tab, "pure-base-context.css")
    injectCss(tab, "pure-grids.css")
    injectCss(tab, "pure-tables.css")
    injectContentScriptFile(tab,"kotlin.js")
    injectContentScriptFile(tab,"declarations.js")
    injectContentScriptFile(tab,"kotlinx-html-js.js")
    injectContentScriptFile(tab,"kotlinx-serialization-runtime-js.js")
    injectContentScriptFile(tab,"kotlinx-coroutines-core.js")
    injectContentScriptFile(tab, "core.js")
    injectContentScriptFile(tab,"content_script.js")
}

fun sendBrowserNotification(message : String) {
    browser.notifications.create(options = CreateNotificationOptions(
        type = "basic",
        title = "Hover Translation Plugin",
        message = message)
    )
}

suspend fun injectContentScriptFile(tab :Tab, fileName : String)
        = browser.tabs.executeScript(tab.id,InjectDetails(file = "$CONTENT_SCRIPT_PATH/$fileName")).await()

suspend fun injectCss(tab :Tab, fileName : String)
        = browser.tabs.insertCSS(tab.id, InjectDetails(file ="$CSS_PATH/$fileName")).await()