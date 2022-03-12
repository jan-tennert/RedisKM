package io.github.jan.rediskm.json.entities

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.json.params.clearJson
import io.github.jan.rediskm.json.params.deleteJson
import io.github.jan.rediskm.json.params.getJson
import io.github.jan.rediskm.json.params.getJsonType
import io.github.jan.rediskm.json.params.putJson

sealed interface RedisJsonElement {

    val path: String
    val key: String
    val redisClient: RedisClient

    suspend fun delete() = redisClient.deleteJson(key, path)

    suspend fun clear() = redisClient.clearJson(key, path)

    suspend fun getType() = redisClient.getJsonType(key, path)

}

suspend inline fun <reified T> RedisJsonElement.get() = redisClient.getJson<T>(key, path)!!

suspend inline fun <reified T> RedisJsonElement.set(value: T) = redisClient.putJson(this.key, this.path, value)
