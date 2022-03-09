package io.github.jan.rediskm.params.get

import io.github.jan.rediskm.RedisClient
import io.github.jan.rediskm.RedisException
import io.github.jan.rediskm.readResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.typeOf

/**
 * Get the value of [key].
 *
 * @param delete If true, the key will be deleted after getting it.
 * @param extraParams Pass in any additional parameters you want to send to the server.
 */
suspend inline fun <reified T> RedisClient.get(key: String, delete: Boolean = false, extraParams: GetExParams.() -> Unit): T {
    val params = GetExParams(mutableListOf()).apply(extraParams)
    val args = if(delete) {
        listOf("GETDEL", key)
    } else if(params.args.isNotEmpty()) {
        listOf("GETEX", key, *params.args.toTypedArray())
    } else {
        listOf("GET", key)
    }
    sendCommand(*args.toTypedArray())
    val result = rawClient.readResponse()
    return when(T::class) {
        String::class -> result.toString() as T
        Int::class -> result.toString().toInt() as T
        Long::class -> result.toString().toLong() as T
        Float::class -> result.toString().toFloat() as T
        Double::class -> result.toString().toDouble() as T
        else -> {
            val serializer = serializerOrNull(typeOf<T>()) ?: throw RedisException("Unsupported type: ${T::class}")
            Json.decodeFromString(serializer, result.toString().also(::println)) as T
        }
    }
}