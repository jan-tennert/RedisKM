package io.github.jan.rediskm.json.entities

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.json.params.getJson

class RedisJsonObject(override val redisClient: RedisClient, override val key: String, override val path: String) : RedisJsonElement {

    suspend inline fun <reified T> get(key: String): T? {
        val newPath = "$path.$key"
        return redisClient.getJson(this.key, newPath)
    }

    suspend fun getKeys(): Nothing = TODO()

    suspend fun getAmountOfKeys(): Nothing = TODO()



}