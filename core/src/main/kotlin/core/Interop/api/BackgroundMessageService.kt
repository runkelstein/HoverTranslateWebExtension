package core.Interop.api

import core.Interop.commands.CommandBase
import core.utils.jsObject
import kotlinx.coroutines.delay
import runtime.MessageSender
import webextensions.browser
import kotlin.browser.window
import kotlin.js.Date
import kotlin.js.Promise
import kotlin.random.Random

object BackgroundMessageService : IMessageService {

    init {
        browser.runtime.onMessage.addListener(::onMessageReceived)
    }

    // todo: thread safe map required
    private val messageQueue : MutableMap<String, dynamic> = HashMap()

    private fun onMessageReceived(message : dynamic, sender : MessageSender, sendResponse: () -> Unit)
    {
        messageQueue[message.requestId] = message
    }

    override fun <T : CommandBase, R> send(cmd: T): Promise<R> {

        val requestId = createRequestId(cmd);

        return sendCommand(cmd, requestId).then {
            // javascript unwraps nested promises automaticly
            handleResult<R>(cmd, requestId).unsafeCast<R>()
        }
    }

    private fun <T : CommandBase> sendCommand(cmd: T, paramRequestId : String) : Promise<*> {
        val serializedCommand = JSON.stringify(cmd)
        return browser.tabs.sendMessage(cmd.targetId,
            jsObject {
                requestId = paramRequestId
                typeName = cmd.getTypeName()
                message = serializedCommand
            }
        )
    }

    private fun <T : CommandBase> createRequestId(cmd :T) : String {
        return "${cmd.getTypeName()}_${Random.nextInt()}_${Date.now()}"
    }

    private fun <T> handleResult(cmd : CommandBase, paramRequestId: String ) : Promise<T> {
        return Promise { resolve, reject->

//            runBlocking {
//
//                do {
//                    delay(1000)
//                } while(true)
//
//            }
        }
    }

}