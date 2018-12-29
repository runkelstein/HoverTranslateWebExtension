package core.Interop.api

import core.Interop.commands.CommandBase
import kotlin.js.Promise

class ContentMessageService : IMessageService {
    override fun <T : CommandBase, R> send(cmd: T): Promise<R> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}