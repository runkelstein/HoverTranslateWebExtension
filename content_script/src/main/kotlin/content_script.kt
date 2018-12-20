import webextensions.browser
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


fun sendCommandToBackgroundScript(cmd : String){
    browser.runtime.sendMessage(message = jsObject {
        command = cmd
    });
    console.log("Command $cmd was send to background script")
}

inline fun jsObject(init: dynamic.() -> Unit): dynamic {
    val o = js("{}")
    init(o)
    return o
}


