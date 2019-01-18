package com.inspiritious.HoverTranslateWebExtension.core.Interop.commands

abstract class CommandBase(open val targetId : Int = 0)
{
    fun getTypeName() = this::class.js.name
}