import core.Interop.api.ContentMessageService
import core.Interop.api.onReceive
import core.Interop.commands.SwitchPluginCommand
import core.Interop.dto.ResultDto
import kotlin.browser.window

val messageReceiver = ContentMessageService
val overlay = Overlay()

private fun onSwitchPlugin(cmd : SwitchPluginCommand) : ResultDto {
    console.log("received switch plugin with value: " + cmd.isEnabled)
    return ResultDto(true, "DieScheißeGehtNurManchmal")
}

fun main(args: Array<String>) {

    if (window.asDynamic().hasRun == true) {
        return
    }
    window.asDynamic().hasRun = true

    messageReceiver.onReceive(::onSwitchPlugin)

//    browser.runtime.onMessage.addListener { received,_,_ ->
//
//        // todo: better api is required
//        val cmd = JSON.parse(SwitchPluginCommand.serializer(), received.message as String)
//        console.log(!cmd.isEnabled)
//
//        browser.runtime.sendMessage(message = jsObject {
//            message = "DieScheißeGehtNurManchmal"
//            requestId = received.requestId
//        });
//
////        if (message.command === "EnableTranslationPlugin") { // todo: use constants
////            sendCommandToBackgroundScript("TranslationPluginEnabled")
////            overlay.enable()
////        } else if (message.command === "DisableTranslationPlugin") {
////            sendCommandToBackgroundScript("TranslationPluginDisabled")
////            overlay.disable()
////        }
//    }

}







