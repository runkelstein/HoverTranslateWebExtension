package com.runkelstein.hoverTranslateWebExtension.content_script

import com.runkelstein.hoverTranslateWebExtension.core.utils.jsObject
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