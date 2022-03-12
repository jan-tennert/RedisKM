package io.github.jan.rediskm.json.params

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.utils.deserialize
import io.github.jan.rediskm.json.entities.RedisJsonArray
import io.github.jan.rediskm.json.entities.RedisJsonBoolean
import io.github.jan.rediskm.json.entities.RedisJsonNumber
import io.github.jan.rediskm.json.entities.RedisJsonObject
import io.github.jan.rediskm.json.entities.RedisJsonPrimitive
import io.github.jan.rediskm.json.entities.RedisJsonString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend inline fun <reified T> RedisClient.getJson(key: String, path: String = "."): T? {
    return when(T::class) {
        RedisJsonObject::class -> RedisJsonObject(this, key, path) as T
        RedisJsonString::class -> RedisJsonString(this, key, path) as T
        RedisJsonNumber::class -> RedisJsonNumber(this, key, path) as T
        RedisJsonBoolean::class -> RedisJsonBoolean(this, key, path) as T
        RedisJsonArray::class -> RedisJsonArray(this, key, path) as T
        else -> {
            sendCommand("JSON.GET", key, path)
            val result = receive()
            result?.let { deserialize(it) }
        }
    }
}

suspend inline fun <reified T> RedisClient.putJson(key: String, path: String = ".", value: T) {
    sendCommand("JSON.SET", key, path, if(value !is String) Json.encodeToString(value) else value)
    receive()
}

suspend inline fun RedisClient.deleteJson(key: String, path: String = "."): Long {
    sendCommand("JSON.DEL", key, path)
    return receive()!!.value as Long
}

suspend inline fun RedisClient.clearJson(key: String, path: String = "."): Long {
    sendCommand("JSON.CLEAR", key, path)
    return receive()!!.value as Long
}

suspend inline fun RedisClient.getJsonType(key: String, path: String = "."): String {
    sendCommand("JSON.TYPE", key, path)
    return receive()!!.value as String
}

suspend fun RedisClient.increaseByJson(key: String, value: Double, path: String) {
    sendCommand("JSON.NUMINCRBY", key, path, value)
    receive()
}

suspend fun RedisClient.multiplyByJson(key: String, value: Double, path: String) {
    sendCommand("JSON.NUMMULTBY", key, path, value)
    receive()
}

suspend fun RedisClient.toggleJson(key: String, path: String) {
    sendCommand("JSON.TOGGLE", key, path)
    receive()
}

suspend fun RedisClient.appendJson(key: String, value: String, path: String) {
    sendCommand("JSON.STRAPPEND", key, path, value)
    receive()
}

suspend fun RedisClient.lengthJson(key: String, path: String) {
    sendCommand("JSON.STRLEN", key, path)
    receive()
}

//MGET