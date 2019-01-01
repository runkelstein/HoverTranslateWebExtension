package core.Interop.api

import core.Interop.commands.CommandBase
import core.Interop.dto.ResultDto
import core.utils.await
import core.utils.jsObject
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

    private enum class MessageType {
        Response,
        Send
    }

    private sealed class SendAction {
        data class RegisterSender(val requestId : String, val response: CompletableDeferred<dynamic>) : SendAction()
        data class MessageReceived(val requestId : String, val content : dynamic) : SendAction()
    }

    private sealed class ReceiveAction {
        data class RegisterReceiver(val typeName : String, val responseChannel: Channel<Pair<String, dynamic>>) : ReceiveAction()
        data class MessageReceived(val typeName : String, val requestId : String, val content : dynamic) : ReceiveAction()
    }

    private val sendActorChannel = Channel<SendAction>()
    private val receiveActorChannel = Channel<ReceiveAction>()

    init {
        browser.runtime.onMessage.addListener(::onMessageReceived)

        runSendActor();
        runReceiveActor()
    }

    override fun <T : CommandBase, R : Any> send(cmd: T, requestClass : KClass<T>, receiverClass : KClass<R>): Deferred<R> = GlobalScope.async {

        val requestId = createRequestId(cmd);

        val response = CompletableDeferred<dynamic>()
        sendActorChannel.send(SendAction.RegisterSender(requestId, response))

        sendCommand(cmd, requestId, requestClass)
        val result = response.await()
        return@async convertResult(result, receiverClass)

    }

     override fun <T : CommandBase, R : ResultDto> onReceive(action: (T) -> R, commandClass : KClass<T>, resultClass : KClass<R>) {

         GlobalScope.launch {

             val commandName = commandClass.simpleName!!
             val responseChannel = Channel<Pair<String, dynamic>>()
             receiveActorChannel.send(ReceiveAction.RegisterReceiver(commandName, responseChannel))

             for ((requestId, content) in responseChannel) {

                 val cmd = JSON.parse(commandClass.serializer(), content)
                 val result = action(cmd)
                 sendResult(result, requestId, resultClass)

             }

         }

     }


    private fun runSendActor() = GlobalScope.launch {
            val recipients : MutableMap<String, CompletableDeferred<dynamic>> = HashMap()
            for(message in sendActorChannel) {
                when(message) {
                    is SendAction.RegisterSender -> recipients.put(message.requestId, message.response)
                    is SendAction.MessageReceived -> {
                        val response = recipients.remove(message.requestId)
                        response?.complete(message.content);

                    }

                }
            }
        }

    private fun runReceiveActor() = GlobalScope.launch {
        val receivers : MutableMap<String, Channel<Pair<String, dynamic>>> = HashMap()
        for(message in receiveActorChannel) {
            when (message) {
                is ReceiveAction.RegisterReceiver -> receivers.put(message.typeName, message.responseChannel)
                is ReceiveAction.MessageReceived -> receivers[message.typeName]?.send(Pair(message.requestId, message.content))
            }
        }
    }



    private fun onMessageReceived(message : dynamic, sender : MessageSender, sendResponse: () -> Unit)
    {
        GlobalScope.launch {

            if (message.messageType == MessageType.Send.toString()) {
                sendActorChannel.send(SendAction.MessageReceived(message.requestId, message))
            } else {
                receiveActorChannel.send(ReceiveAction.MessageReceived(message.typeName, message.requestId, message.content))
            }

        }
    }

    protected abstract suspend fun send(message : String, targetId : Int);

    private suspend fun <T : CommandBase> sendCommand(cmd: T, paramRequestId : String, klass : KClass<T>)  {
        val serializedCommand = JSON.stringify(klass.serializer(), cmd)
        val msg = message(paramRequestId, cmd.getTypeName(), serializedCommand, MessageType.Send);

        send(msg, cmd.targetId)
    }

    private suspend fun <R : ResultDto> sendResult(result: R, paramRequestId : String, klass : KClass<R>)  {
        val serializedResult = JSON.stringify(klass.serializer(), result)
        val msg = message(paramRequestId, klass.js.name, serializedResult, MessageType.Response);

        send(msg, 0) // todo: figure out target id
    }

    private fun <T : CommandBase> createRequestId(cmd :T) : String {
        return "${cmd.getTypeName()}_${Random.nextInt()}_${Date.now()}"
    }

    private fun <R: Any> convertResult(received : dynamic, klass : KClass<R>) : R {
        return JSON.parse(klass.serializer(), received.content as String)
    }

    private fun message(pRequestId : String, pTypeName: String, pContent : String, pMessageType: MessageType) : dynamic {
        return jsObject {
            requestId = pRequestId
            typeName = pTypeName
            content = pContent
            messageType = pMessageType.toString()
        }
    }

}