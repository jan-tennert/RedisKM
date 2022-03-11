package io.github.jan.rediskm.json.params

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.utils.deserialize
import io.github.jan.rediskm.json.entities.RedisJsonObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

suspend inline fun <reified T> RedisClient.getJson(key: String, path: String = "."): T? {
    return when(T::class) {
        RedisJsonObject::class -> RedisJsonObject(this, key, path) as T
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

suspend inline fun RedisClient.getType(key: String, path: String = "."): String {
    sendCommand("JSON.TYPE", key, path)
    return receive()!!.value as String
}

//MGET