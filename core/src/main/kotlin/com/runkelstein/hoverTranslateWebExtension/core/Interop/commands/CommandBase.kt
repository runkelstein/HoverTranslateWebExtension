package com.runkelstein.hoverTranslateWebExtension.core.Interop.commands


interface CommandBase
{
    val targetId : Int
    fun getTypeName() = this::class.js.name
}