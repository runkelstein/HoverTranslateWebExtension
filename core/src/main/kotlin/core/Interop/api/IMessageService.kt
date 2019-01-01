package core.Interop.api

import core.Interop.commands.CommandBase
import core.Interop.dto.ResultDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlin.js.Promise
import kotlin.reflect.KClass

interface IMessageService {

    fun <T : CommandBase, R : Any> send(cmd: T, requestClass : KClass<T>, receiverClass : KClass<R>): Deferred<R>
    fun <T : CommandBase, R : ResultDto>onReceive(action : (T) -> R, commandClass : KClass<T>, resultClass : KClass<R>)
}

inline fun <reified T : CommandBase, R: Any> IMessageService.send(cmd: T, receiverClass : KClass<R>): Deferred<R> = send(cmd, T::class, receiverClass)
inline fun <reified T : CommandBase, reified R : ResultDto>IMessageService.onReceive(noinline action : (T) -> R) = onReceive(action, T::class, R::class)