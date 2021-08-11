package com.moefactory.bettermiuiexpress.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object IntAsBooleanSerializer : KSerializer<Boolean> {
    override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeLong(if (value) 1 else 0)
    }

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeInt() == 1
    }
}