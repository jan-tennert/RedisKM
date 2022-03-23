package io.github.jan.rediskm.json.params

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.RedisModule
import io.github.jan.rediskm.core.utils.deserialize
import io.github.jan.rediskm.json.entities.RedisJsonArray
import io.github.jan.rediskm.json.entities.RedisJsonBoolean
import io.github.jan.rediskm.json.entities.RedisJsonNumber
import io.github.jan.rediskm.json.entities.RedisJsonObject
import io.github.jan.rediskm.json.entities.RedisJsonString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonModule internal constructor(val client: RedisClient) : RedisModule

val RedisClient.json: JsonModule
    get() {
        return if(modules.containsKey("json")) {
            modules["json"] as JsonModule
        } else JsonModule(this).also {
            modules["json"] = it
        }
    }

suspend inline fun <reified T> JsonModule.get(key: String, path: String = "."): T? {
    return when(T::class) {
        RedisJsonObject::class -> RedisJsonObject(client, key, path) as T
        RedisJsonString::class -> RedisJsonString(client, key, path) as T
        RedisJsonNumber::class -> RedisJsonNumber(client, key, path) as T
        RedisJsonBoolean::class -> RedisJsonBoolean(client, key, path) as T
        RedisJsonArray::class -> RedisJsonArray(client, key, path) as T
        else -> {
            client.sendCommand("JSON.GET", key, path)
            val result = client.receive()
            result?.let { deserialize(it) }
        }
    }
}

suspend inline fun <reified T> JsonModule.put(key: String, path: String = ".", value: T) {
    client.sendCommand("JSON.SET", key, path, if(value !is String) Json.encodeToString(value) else value)
    client.receive()
}

suspend inline fun JsonModule.delete(key: String, path: String = "."): Long {
    client.sendCommand("JSON.DEL", key, path)
    return client.receive()!!.value as Long
}

suspend inline fun JsonModule.clear(key: String, path: String = "."): Long {
    client.sendCommand("JSON.CLEAR", key, path)
    return client.receive()!!.value as Long
}

suspend inline fun JsonModule.getJsonType(key: String, path: String = "."): String {
    client.sendCommand("JSON.TYPE", key, path)
    return client.receive()!!.value as String
}

suspend fun JsonModule.increaseNumberBy(key: String, value: Double, path: String) {
    client.sendCommand("JSON.NUMINCRBY", key, path, value)
    client.receive()
}

suspend fun JsonModule.multiplyNumberBy(key: String, value: Double, path: String) {
    client.sendCommand("JSON.NUMMULTBY", key, path, value)
    client.receive()
}

suspend fun JsonModule.toggleBoolean(key: String, path: String) {
    client.sendCommand("JSON.TOGGLE", key, path)
    client.receive()
}

suspend fun JsonModule.appendString(key: String, value: String, path: String) {
    client.sendCommand("JSON.STRAPPEND", key, path, value)
    client.receive()
}

suspend fun JsonModule.stringLength(key: String, path: String) {
    client.sendCommand("JSON.STRLEN", key, path)
    client.receive()
}

//MGET