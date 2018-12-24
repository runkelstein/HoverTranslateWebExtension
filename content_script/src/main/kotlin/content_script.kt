import kotlinx.html.dom.create
import kotlinx.html.p
import webextensions.browser
import kotlin.browser.document
import kotlin.browser.window

val overlay = Overlay()

fun main(args: Array<String>) {

    if (window.asDynamic().hasRun == true) {
        return
    }
    window.asDynamic().hasRun = true

    console.log("translation plugin installed")

    browser.runtime.onMessage.addListener { message,_,_ ->

        if (message.command === "EnableTranslationPlugin") { // todo: use constants
            sendCommandToBackgroundScript("TranslationPluginEnabled")
            overlay.enable()
        } else if (message.command === "DisableTranslationPlugin") {
            sendCommandToBackgroundScript("TranslationPluginDisabled")
            overlay.disable()
        }
    }
}







