/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.runreport

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Created by Tarun on 03-08-17.
 */
@Serializable
data class DataRow(
    @Serializable(with = RowDeserializer::class)
    val row: List<String?> = emptyList(),
)
object RowDeserializer : KSerializer<List<String?>> {

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        ListSerializer(String.serializer().nullable).descriptor

    override fun deserialize(decoder: Decoder): List<String?> {
        val jsonElement = (decoder as? JsonDecoder)?.decodeJsonElement()
            ?: throw SerializationException("Decoder is not a JsonDecoder")

        return read(jsonElement)
    }

    private fun read(jsonElement: JsonElement): List<String?> {
        return when (jsonElement) {
            is JsonArray -> {
                jsonElement.map { element ->
                    when (element) {
                        is JsonPrimitive -> element.contentOrNull
                        is JsonArray -> element.joinToString("-") {
                            it.jsonPrimitive.content
                        }
                        else -> element.toString()
                    }
                }
            }

            else -> throw SerializationException("Expected JsonArray, got $jsonElement")
        }
    }

    override fun serialize(encoder: Encoder, value: List<String?>) {
        if (encoder !is JsonEncoder) {
            throw SerializationException("This serializer can only be used with Json")
        }

        val jsonArray = JsonArray(
            value.map {
                when {
                    it == null -> JsonNull
                    else -> JsonPrimitive(it)
                }
            },
        )

        encoder.encodeJsonElement(jsonArray)
    }
}
