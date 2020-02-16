package com.runkelstein.hoverTranslateWebExtension.core.Interop.api

import com.runkelstein.hoverTranslateWebExtension.core.utils.await
import webextensions.browser

object ContentMessageService : AbstractMessageService() {

    override suspend fun send(message: String, targetId: Int) = browser.runtime.sendMessage(message = message).await();

}