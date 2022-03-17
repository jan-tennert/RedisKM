package io.github.jan.rediskm.json.entities

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.json.params.getJson

class RedisJsonObject(override val redisClient: RedisClient, override val key: String, override val path: String) : RedisJsonElement {

    suspend inline fun <reified T> getOrNull(key: String): T? {
        val newPath = "$path.$key"
        return redisClient.getJson(this.key, newPath)
    }

    suspend inline fun <reified T> get(key: String) = getOrNull<T>(key)!!

    suspend inline fun <reified T> getOrDefault(key: String, default: T) = getOrNull(key) ?: default

    suspend fun getKeys(): Nothing = TODO()

    suspend fun getAmountOfKeys(): Nothing = TODO()



}