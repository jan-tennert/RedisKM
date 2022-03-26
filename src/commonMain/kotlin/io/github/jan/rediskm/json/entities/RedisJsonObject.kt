package io.github.jan.rediskm.json.entities

import com.soywiz.kds.fastCastTo
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisIntegerValue
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.json.params.get
import io.github.jan.rediskm.json.params.json
import io.github.jan.rediskm.json.params.put

class RedisJsonObject(override val redisClient: RedisClient, override val key: String, override val path: String) : RedisJsonElement {

    suspend inline fun <reified T> getOrNull(key: String): T? {
        val newPath = "$path.$key"
        return redisClient.json.get(this.key, newPath)
    }

    suspend inline fun <reified T> get(key: String) = getOrNull<T>(key)!!

    suspend inline fun <reified T> getOrDefault(key: String, default: T) = getOrNull(key) ?: default

    suspend inline fun <reified T> put(key: String, value: T) {
        val newPath = "$path.$key"
        redisClient.json.put(this.key, newPath, value)
    }

    suspend fun getKeys(): List<String> {
        redisClient.sendCommand("JSON.OBJKEYS", key, path)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList()
    }

    suspend fun getAmountOfKeys() : Long {
        redisClient.sendCommand("JSON.OBJLEN", key, path)
        return (redisClient.receive() as? RedisIntegerValue)?.value ?: 0
    }

}