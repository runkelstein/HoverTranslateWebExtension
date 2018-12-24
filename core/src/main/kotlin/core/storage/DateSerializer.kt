package core.storage

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlin.js.Date

@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date> {
    override fun deserialize(input: Decoder): Date {
        return Date(input.decodeDouble())
    }

    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("Date")

    override fun serialize(output: Encoder, obj: Date) {
        output.encodeDouble(obj.getTime())
    }
}