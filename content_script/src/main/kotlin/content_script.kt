import core.Interop.api.ContentMessageService
import core.Interop.api.onReceive
import core.Interop.commands.SwitchPluginCommand
import core.Interop.dto.resultWithSucces
import core.Interop.dto.SimpleResultDto
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







