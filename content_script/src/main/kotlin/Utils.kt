import core.utils.jsObject
import webextensions.browser

fun sendCommandToBackgroundScript(cmd : String){
    browser.runtime.sendMessage(message = jsObject {
        command = cmd
    });
}

fun sendCommandToBackgroundScript(cmd : String, text: String){
    browser.runtime.sendMessage(message = jsObject {
        command = cmd; data = text
    });
}