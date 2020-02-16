package com.runkelstein.hoverTranslateWebExtension.core.Interop.commands

import kotlinx.serialization.Serializable

@Serializable
data class SwitchPluginCommand(override val targetId : Int, val isEnabled : Boolean) : CommandBase