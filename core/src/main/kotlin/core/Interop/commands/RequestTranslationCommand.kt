package core.Interop.commands

import kotlinx.serialization.Serializable

@Serializable
data class RequestTranslationCommand(val searchTerm : String) : CommandBase()