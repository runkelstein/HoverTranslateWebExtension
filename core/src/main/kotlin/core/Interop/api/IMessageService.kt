package core.Interop.api

import core.Interop.commands.CommandBase
import kotlin.js.Promise

interface IMessageService {

    fun <T : CommandBase, R> send(cmd: T): Promise<R>

}