package com.runkelstein.hoverTranslateWebExtension.core.Interop.commands

import kotlinx.serialization.Serializable

@Serializable
data class RequestTranslationCommand(val searchTerm : String, override val targetId : Int = 0) : CommandBase