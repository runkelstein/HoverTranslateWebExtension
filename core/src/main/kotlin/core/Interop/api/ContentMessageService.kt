package com.inspiritious.HoverTranslateWebExtension.core.Interop.api

import com.inspiritious.HoverTranslateWebExtension.core.utils.await
import webextensions.browser

object ContentMessageService : AbstractMessageService() {

    override suspend fun send(message: String, targetId: Int) = browser.runtime.sendMessage(message = message).await();

}