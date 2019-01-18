package com.inspiritious.HoverTranslateWebExtension.core.Interop.api

import com.inspiritious.HoverTranslateWebExtension.core.utils.await
import webextensions.browser

object BackgroundMessageService : AbstractMessageService() {

    override suspend fun send(message: String, targetId: Int) = browser.tabs.sendMessage(targetId, message).await()


}