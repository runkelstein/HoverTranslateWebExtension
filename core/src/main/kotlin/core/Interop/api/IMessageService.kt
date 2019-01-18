package com.inspiritious.HoverTranslateWebExtension.core.Interop.api

import com.inspiritious.HoverTranslateWebExtension.core.Interop.commands.CommandBase
import com.inspiritious.HoverTranslateWebExtension.core.Interop.dto.ResultDto
import kotlinx.coroutines.Deferred
import kotlin.reflect.KClass

interface IMessageService {

    fun <T : CommandBase, R : Any> send(cmd: T, requestClass : KClass<T>, receiverClass : KClass<R>): Deferred<ResultDto<R>>
    fun <T : CommandBase, R : Any>onReceive(action : (T) -> ResultDto<R>, commandClass : KClass<T>, resultClass : KClass<R>)
}

inline fun <reified T : CommandBase, reified R : Any> IMessageService.send(cmd: T): Deferred<ResultDto<R>> = send(cmd, T::class, R::class)
inline fun <reified T : CommandBase, reified R : Any> IMessageService.onReceive(noinline action : (T) -> ResultDto<R>) = onReceive(action, T::class, R::class)