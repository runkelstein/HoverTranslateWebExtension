package com.inspiritious.HoverTranslateWebExtension.content_script

import com.inspiritious.HoverTranslateWebExtension.core.Interop.api.ContentMessageService
import com.inspiritious.HoverTranslateWebExtension.core.Interop.api.onReceive
import com.inspiritious.HoverTranslateWebExtension.core.Interop.commands.SwitchPluginCommand
import com.inspiritious.HoverTranslateWebExtension.core.Interop.dto.resultWithSucces
import com.inspiritious.HoverTranslateWebExtension.core.Interop.dto.SimpleResultDto
import kotlin.browser.window

val overlay = Overlay()

private fun onSwitchPlugin(cmd : SwitchPluginCommand) : SimpleResultDto {

    console.log("received switch plugin with value: " + cmd.isEnabled)
    if (cmd.isEnabled) {
        overlay.enable()
    } else  {
        overlay.disable()
    }

    return resultWithSucces()
}

fun main(args: Array<String>) {

    if (window.asDynamic().hasRun == true) {
        return
    }
    window.asDynamic().hasRun = true

    ContentMessageService.onReceive(::onSwitchPlugin)
}







