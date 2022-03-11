package io.github.jan.rediskm.core.utils

import io.github.jan.rediskm.core.RedisException
import io.github.jan.rediskm.core.entities.RedisValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.typeOf

inline fun <reified T> serialize(value: T): String {
    return when(value) {
        is Number -> value.toString()
        is String -> value.toString()
        else -> {
            val serializer = serializerOrNull(typeOf<T>()) ?: throw RedisException("Unsupported type: ${T::class}")
            Json.encodeToString(serializer, value)
        }
    }
}

inline fun <reified T> deserialize(value: RedisValue<*>): T {
    return when(T::class) {
        String::class -> value.value.toString() as T
        Int::class -> value.value.toString().toInt() as T
        Long::class -> value.value.toString().toLong() as T
        Float::class -> value.value.toString().toFloat() as T
        Double::class -> value.value.toString().toDouble() as T
        else -> {
            val serializer = serializerOrNull(typeOf<T>()) ?: throw RedisException("Unsupported type: ${T::class}")
            Json.decodeFromString(serializer, value.value.toString()) as T
        }
    }
}