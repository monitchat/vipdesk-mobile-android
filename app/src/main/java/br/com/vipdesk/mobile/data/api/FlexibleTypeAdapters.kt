package br.com.vipdesk.mobile.data.api

import br.com.vipdesk.mobile.data.model.Client
import br.com.vipdesk.mobile.data.model.User
import com.google.gson.*
import com.google.gson.internal.LazilyParsedNumber
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type

/**
 * Handles User fields that can come as either an Object or a primitive (String/Number).
 */
class FlexibleUserDeserializer : JsonDeserializer<User?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): User? {
        if (json == null || json.isJsonNull) return null

        return try {
            when {
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    User(
                        id = obj.safeInt("id") ?: 0,
                        name = obj.safeString("name") ?: "",
                        email = obj.safeString("email") ?: "",
                        avatar = obj.safeString("avatar"),
                        companyId = obj.safeInt("company_id"),
                        departmentId = obj.safeInt("department_id"),
                        role = obj.safeString("role")
                    )
                }
                json.isJsonPrimitive -> {
                    val value = json.asString
                    if (value.isNullOrBlank()) null
                    else User(id = 0, name = value, email = "")
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * Handles Client fields that can come as Object, Array, String or null.
 */
class FlexibleClientDeserializer : JsonDeserializer<Client?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Client? {
        if (json == null || json.isJsonNull) return null

        return try {
            when {
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    Client(
                        id = obj.safeInt("id"),
                        name = obj.safeString("name")
                    )
                }
                json.isJsonArray -> {
                    // API returns [] instead of object - ignore
                    null
                }
                json.isJsonPrimitive -> {
                    val value = json.asString
                    if (value.isNullOrBlank()) null
                    else Client(id = null, name = value)
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * TypeAdapter for Int that handles empty strings and nulls gracefully.
 * Returns null instead of throwing NumberFormatException.
 */
class SafeIntAdapter : TypeAdapter<Int?>() {
    override fun write(out: JsonWriter, value: Int?) {
        if (value == null) out.nullValue() else out.value(value)
    }

    override fun read(reader: JsonReader): Int? {
        return when (reader.peek()) {
            JsonToken.NULL -> { reader.nextNull(); null }
            JsonToken.NUMBER -> {
                try { reader.nextInt() } catch (_: Exception) { reader.nextString(); null }
            }
            JsonToken.STRING -> {
                val s = reader.nextString()
                s.toIntOrNull()
            }
            JsonToken.BOOLEAN -> { reader.nextBoolean(); null }
            else -> { reader.skipValue(); null }
        }
    }
}

/**
 * TypeAdapter for Long that handles empty strings and nulls gracefully.
 */
class SafeLongAdapter : TypeAdapter<Long?>() {
    override fun write(out: JsonWriter, value: Long?) {
        if (value == null) out.nullValue() else out.value(value)
    }

    override fun read(reader: JsonReader): Long? {
        return when (reader.peek()) {
            JsonToken.NULL -> { reader.nextNull(); null }
            JsonToken.NUMBER -> {
                try { reader.nextLong() } catch (_: Exception) { reader.nextString(); null }
            }
            JsonToken.STRING -> {
                val s = reader.nextString()
                s.toLongOrNull()
            }
            else -> { reader.skipValue(); null }
        }
    }
}

private fun JsonObject.safeString(key: String): String? {
    val el = get(key) ?: return null
    if (el.isJsonNull) return null
    return try { el.asString.ifBlank { null } } catch (_: Exception) { null }
}

private fun JsonObject.safeInt(key: String): Int? {
    val el = get(key) ?: return null
    if (el.isJsonNull) return null
    return try {
        if (el.isJsonPrimitive) {
            val prim = el.asJsonPrimitive
            if (prim.isNumber) prim.asInt
            else prim.asString.toIntOrNull()
        } else null
    } catch (_: Exception) { null }
}
