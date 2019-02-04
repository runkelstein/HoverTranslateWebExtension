package com.inspiritious.HoverTranslateWebExtension.background_script

import browserAction.Details3
import com.inspiritious.HoverTranslateWebExtension.core.Interop.api.BackgroundMessageService
import com.inspiritious.HoverTranslateWebExtension.core.Interop.api.IMessageService
import com.inspiritious.HoverTranslateWebExtension.core.Interop.api.send
import com.inspiritious.HoverTranslateWebExtension.core.Interop.commands.RequestTranslationCommand
import com.inspiritious.HoverTranslateWebExtension.core.Interop.commands.SwitchPluginCommand
import com.inspiritious.HoverTranslateWebExtension.core.Interop.dto.DeferredSimpleResult
import com.inspiritious.HoverTranslateWebExtension.core.Interop.dto.ResultDto
import com.inspiritious.HoverTranslateWebExtension.core.Interop.dto.resultWithSuccess
import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.DictCC
import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.IDictionary
import com.inspiritious.HoverTranslateWebExtension.core.dictionaryLib.SearchResult
import com.inspiritious.HoverTranslateWebExtension.core.storage.StorageService
import com.inspiritious.HoverTranslateWebExtension.core.utils.await
import extensionTypes.InjectDetails
import notifications.CreateNotificationOptions
import tabs.Tab
import webextensions.browser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tabs.ActiveInfo
import tabs.ChangeInfo
import com.inspiritious.HoverTranslateWebExtension.core.Interop.api.onReceive

var isActive = false;

const val CONTENT_SCRIPT_PATH = "../content_script"
const val CSS_PATH = "../css"
const val ICONS_PATH = "../img"

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
        browser.browserAction.setIcon(Details3(path = "$ICONS_PATH/icon_128x128_toolbar_active.png"))
    } else {
        browser.browserAction.setIcon(Details3(path = "$ICONS_PATH/icon_128x128_toolbar.png"))
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

fun loadDictionary() = GlobalScope.launch {


    val metaData = StorageService.loadMetadata()
    if (metaData.activated == null) {
        return@launch
    }

    val entry = StorageService.load(metaData.activated!!);
    if (entry!=null) {
        dictionary = DictCC(entry.content)
        val testWord = dictionary.findTranslations("vacanza")
        sendBrowserNotification("Dictionary initialized: " + testWord.results.first().targetLangText)
    } else {
        sendBrowserNotification("Dictionary not initialized: ")
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

    injectCss(tab, "pure-base-context.css", "pure-grids.css", "pure-tables.css")
    injectContentScriptFile(tab,"kotlin.js", "declarations.js",
        "kotlinx-html-js.js","kotlinx-serialization-runtime-js.js","kotlinx-coroutines-core.js",
        "core.js","content_script.js")
}

fun sendBrowserNotification(message : String) {
    browser.notifications.create(options = CreateNotificationOptions(
        type = "basic",
        title = "Hover Translation Plugin",
        message = message)
    )
}

suspend fun injectContentScriptFile(tab :Tab, vararg fileNames : String)
        = fileNames.forEach {  browser.tabs.executeScript(tab.id,InjectDetails(file = "$CONTENT_SCRIPT_PATH/$it")).await() }

suspend fun injectCss(tab :Tab, vararg fileNames : String)
        = fileNames.forEach { browser.tabs.insertCSS(tab.id, InjectDetails(file ="$CSS_PATH/$it")).await()}