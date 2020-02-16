package com.runkelstein.hoverTranslateWebExtension.content_script

import com.runkelstein.hoverTranslateWebExtension.core.Interop.api.ContentMessageService
import com.runkelstein.hoverTranslateWebExtension.core.Interop.api.onReceive
import com.runkelstein.hoverTranslateWebExtension.core.Interop.commands.SwitchPluginCommand
import com.runkelstein.hoverTranslateWebExtension.core.Interop.dto.resultWithSucces
import com.runkelstein.hoverTranslateWebExtension.core.Interop.dto.SimpleResultDto
import kotlin.browser.window

val overlay = Overlay()

private suspend fun onSwitchPlugin(cmd : SwitchPluginCommand) : SimpleResultDto {

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

    ContentMessageService.onReceive { cmd : SwitchPluginCommand -> onSwitchPlugin(cmd) }
}







