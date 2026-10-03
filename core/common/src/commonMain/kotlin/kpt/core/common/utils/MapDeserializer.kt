/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.mapSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object MapDeserializer : KSerializer<Map<String, Any>> {

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        mapSerialDescriptor(String.serializer().descriptor, JsonElement.serializer().descriptor)

    override fun deserialize(decoder: Decoder): Map<String, Any> {
        val jsonElement = (decoder as? JsonDecoder)?.decodeJsonElement()
            ?: throw SerializationException("Decoder is not a JsonDecoder")

        val result = read(jsonElement)
        if (result is Map<*, *>) {
            if (result.keys.all { it is String }) {
                @Suppress("UNCHECKED_CAST")
                return result as Map<String, Any>
            } else {
                throw SerializationException("Map contains non-String keys: $result")
            }
        } else {
            throw SerializationException("Expected a JSON object for Map<String, Any>, got: $result")
        }
    }

    fun read(jsonElement: JsonElement): Any? {
        return when (jsonElement) {
            is JsonArray -> {
                val list = jsonElement.jsonArray.map { read(it) }
                list
            }
            is JsonObject -> {
                jsonElement.jsonObject.entries.associate { (key, value) -> key to read(value) }
            }
            is JsonPrimitive -> {
                when {
                    jsonElement.booleanOrNull != null -> jsonElement.boolean
                    jsonElement.isString -> jsonElement.content
                    jsonElement.doubleOrNull != null -> {
                        val num = jsonElement.double
                        if (kotlin.math.ceil(num) == num.toLong().toDouble()) num.toLong() else num
                    }
                    else -> null
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Map<String, Any>) {
        if (encoder !is JsonEncoder) {
            throw SerializationException("This serializer can only be used with Json")
        }

        val jsonObject = JsonObject(value.mapValues { (_, v) -> write(v) })
        encoder.encodeJsonElement(jsonObject)
    }

    private fun write(value: Any?): JsonElement {
        return when (value) {
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                JsonObject((value as Map<String, Any>).mapValues { (_, entryValue) -> write(entryValue) })
            }
            is List<*> -> JsonArray(value.map { write(it) })
            null -> JsonNull
            else -> JsonPrimitive(value.toString())
        }
    }
}
