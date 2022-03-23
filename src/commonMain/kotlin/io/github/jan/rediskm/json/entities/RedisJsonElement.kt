package io.github.jan.rediskm.json.entities

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.json.params.clear
import io.github.jan.rediskm.json.params.delete
import io.github.jan.rediskm.json.params.get
import io.github.jan.rediskm.json.params.getJsonType
import io.github.jan.rediskm.json.params.json
import io.github.jan.rediskm.json.params.put

sealed interface RedisJsonElement {

    val path: String
    val key: String
    val redisClient: RedisClient

    suspend fun delete() = redisClient.json.delete(key, path)

    suspend fun clear() = redisClient.json.clear(key, path)

    suspend fun getType() = redisClient.json.getJsonType(key, path)

}

suspend inline fun <reified T> RedisJsonElement.get() = redisClient.json.get<T>(key, path)!!

suspend inline fun <reified T> RedisJsonElement.set(value: T) = redisClient.json.put(this.key, this.path, value)
