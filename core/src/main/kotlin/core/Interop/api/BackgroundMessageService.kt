package core.Interop.api

import core.utils.await
import webextensions.browser

object BackgroundMessageService : AbstractMessageService() {

    override suspend fun send(message: String, targetId: Int) = browser.tabs.sendMessage(targetId, message).await()


}