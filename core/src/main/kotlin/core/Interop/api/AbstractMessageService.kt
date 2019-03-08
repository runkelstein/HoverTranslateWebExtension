package com.inspiritious.HoverTranslateWebExtension.core.Interop.api

import com.inspiritious.HoverTranslateWebExtension.core.Interop.commands.CommandBase
import com.inspiritious.HoverTranslateWebExtension.core.Interop.dto.ResultDto
import com.inspiritious.HoverTranslateWebExtension.core.utils.jsObject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.serializer
import runtime.MessageSender
import webextensions.browser
import kotlin.js.Date
import kotlin.random.Random
import kotlin.reflect.KClass

@UseExperimental(ImplicitReflectionSerializer::class)
abstract class AbstractMessageService : IMessageService {

    private enum class MessageOriginType {
        Receiver,
        Sender
    }

    private sealed class SendAction {
        data class RegisterSender(val requestId : String, val response: CompletableDeferred<dynamic>) : SendAction()
        data class MessageReceived(val requestId : String, val payload : dynamic) : SendAction()
    }

    private class SenderMessage(val requestId : String, val senderId : Int, val command : dynamic)

    private sealed class ReceiveAction {
        data class RegisterReceiver(val typeName : String, val responseChannel: Channel<SenderMessage>) : ReceiveAction()
        data class MessageReceived(val typeName : String, val message : SenderMessage) : ReceiveAction()
    }

    private val sendActorChannel = Channel<SendAction>()
    private val receiveActorChannel = Channel<ReceiveAction>()

    init {
        browser.runtime.onMessage.addListener(::onMessageReceived)

        runSendActor();
        runReceiveActor()
    }

    override fun <T : CommandBase, R : Any> send(cmd: T, requestClass : KClass<T>, receiverClass : KClass<R>): Deferred<ResultDto<R>> = GlobalScope.async {

        val requestId = createRequestId(cmd);

        val response = CompletableDeferred<dynamic>()
        sendActorChannel.send(SendAction.RegisterSender(requestId, response))

        sendCommand(cmd, requestId, requestClass)
        val result = response.await()
        return@async parseResult(result, receiverClass)

    }

     override fun <T : CommandBase, R : Any> onReceive(action: suspend (T) -> ResultDto<R>, commandClass : KClass<T>, resultClass : KClass<R>) {

         GlobalScope.launch {

             val commandName = commandClass.simpleName!!
             val responseChannel = Channel<SenderMessage>()
             receiveActorChannel.send(ReceiveAction.RegisterReceiver(commandName, responseChannel))

             for (message in responseChannel) {

                 val cmd = JSON.parse(commandClass.serializer(), message.command)
                 val result = action(cmd)
                 sendResult(result, message.requestId, resultClass, message.senderId)

             }

         }

     }

    private fun runSendActor() = GlobalScope.launch {
            val recipients = HashMap<String, CompletableDeferred<dynamic>>()
            for(action in sendActorChannel) {
                when (action) {
                    is SendAction.RegisterSender -> recipients.put(action.requestId, action.response)
                    is SendAction.MessageReceived -> recipients.remove(action.requestId)?.complete(action.payload);
                }
            }
    }

    private fun runReceiveActor() = GlobalScope.launch {
        val receivers = HashMap<String, Channel<SenderMessage>>()
        for(action in receiveActorChannel) {
            when (action) {
                is ReceiveAction.RegisterReceiver -> receivers.put(action.typeName, action.responseChannel)
                is ReceiveAction.MessageReceived -> receivers[action.typeName]?.send(action.message)
            }
        }
    }

    private fun onMessageReceived(message : dynamic, sender : MessageSender, sendResponse: () -> Unit)
    {
        GlobalScope.launch {

            if (message.origin == MessageOriginType.Receiver.toString()) {
                sendActorChannel.send(SendAction.MessageReceived(message.requestId, message))
            } else {
                receiveActorChannel.send(ReceiveAction.MessageReceived(message.typeName, SenderMessage(message.requestId, sender.tab?.id ?: 0, message.payload)))
            }

        }
    }

    protected abstract suspend fun send(message : String, targetId : Int);

    private suspend fun <T : CommandBase> sendCommand(cmd: T, paramRequestId : String, klass : KClass<T>)  {
        val serializedCommand = JSON.stringify(klass.serializer(), cmd)
        val msg = message(paramRequestId, cmd.getTypeName(), serializedCommand, MessageOriginType.Sender);
        send(msg, cmd.targetId)
    }

    private suspend fun <R : Any> sendResult(result: ResultDto<R>, paramRequestId : String, klass : KClass<R>, targetId : Int)  {
        val serializedResult = JSON.stringify(klass.serializer(), result.payload)
        val msg = resultMessage(paramRequestId, klass.js.name, serializedResult, result);
        send(msg, targetId)
    }

    private fun <T : CommandBase> createRequestId(cmd :T) : String {
        return "${cmd.getTypeName()}_${Random.nextInt()}_${Date.now()}"
    }

    private fun <R: Any> parseResult(received : dynamic, klass : KClass<R>) : ResultDto<R> {
        val data = JSON.parse(klass.serializer(), received.payload as String)
        return ResultDto(received.isSuccess, received.error, data)

    }

    private fun message(pRequestId : String, pTypeName: String, pPayload : String, pOrigin: MessageOriginType) : dynamic {
        return jsObject {
            requestId = pRequestId
            typeName = pTypeName
            payload = pPayload
            origin = pOrigin.toString()
        }
    }

    private fun resultMessage(pRequestId : String, pTypeName: String, pPayload : String,
                              pResult : ResultDto<*>) : dynamic {
        with(message(pRequestId, pTypeName, pPayload, MessageOriginType.Receiver)) {
            isSuccess = pResult.isSuccess
            error = pResult.error
            return this
        }
    }

}