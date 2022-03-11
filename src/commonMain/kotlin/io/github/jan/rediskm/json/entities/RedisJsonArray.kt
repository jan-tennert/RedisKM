package io.github.jan.rediskm.json.entities

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.entities.collection.RedisCollection
import io.github.jan.rediskm.core.utils.serialize
import io.github.jan.rediskm.json.params.getJson

class RedisJsonArray(override val redisClient: RedisClient, override val key: String, override val path: String) : RedisJsonElement {

    suspend inline fun <reified T> get(index: Int): T? {
        val newPath = "$path[$index]"
        return redisClient.getJson(key, newPath)
    }

    suspend inline fun <reified T> add(vararg elements: T): RedisListValue {
        redisClient.sendCommand("JSON.ARRAPPEND", key, path, *elements.map(::serialize).toTypedArray())
        return redisClient.receive() as RedisListValue
    }

    suspend inline fun <reified T> insert(index: Int, vararg elements: T): RedisListValue {
        redisClient.sendCommand("JSON.ARRINSERT", key, path, index, *elements.map(::serialize).toTypedArray())
        return redisClient.receive() as RedisListValue
    }

    suspend fun size(): Long {
        redisClient.sendCommand("JSON.ARRLEN", key, path)
        return (redisClient.receive()?.value as? Long) ?: 0
    }

    suspend fun pop(): Nothing = TODO()

    suspend fun trim(): Nothing = TODO()

}