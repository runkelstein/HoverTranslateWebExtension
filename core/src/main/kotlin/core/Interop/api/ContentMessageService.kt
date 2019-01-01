package core.Interop.api

import core.utils.await
import webextensions.browser

object ContentMessageService : AbstractMessageService() {

    override suspend fun send(message: String, targetId: Int) = browser.runtime.sendMessage(message = message).await();

}