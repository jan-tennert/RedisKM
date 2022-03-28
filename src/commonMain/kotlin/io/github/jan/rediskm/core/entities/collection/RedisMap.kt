package io.github.jan.rediskm.core.entities.collection

import com.soywiz.kds.fastCastTo
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisElement
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.entities.RedisObject

class RedisMap internal constructor(override val redisClient: RedisClient, override val key: String) : RedisObject<Map<String, String>>, RedisElement {

    override suspend fun get(): Map<String, String> {
        redisClient.sendCommand("HGETALL", key)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList().chunked(2) { (key, value) -> key to value }.toMap()
    }

    suspend fun remove(vararg elements: String): Long {
        redisClient.sendCommand("HDEL", key, *elements)
        return redisClient.receive()!!.value as Long
    }

    suspend fun contains(element: String): Boolean {
        redisClient.sendCommand("HEXISTS", key, element)
        return redisClient.receive()!!.value == 1L
    }

    suspend fun get(key: String): String? {
        redisClient.sendCommand("HGET", key)
        return redisClient.receive()?.value?.toString()
    }

    suspend fun getValues(): List<String> {
        redisClient.sendCommand("HVALS", key)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList()
    }

    suspend fun increaseKeyBy(key: String, value: Long): Long {
        redisClient.sendCommand("HINCRBY", key, value)
        return redisClient.receive()!!.value as Long
    }

    suspend fun increaseKeyBy(key: String, value: Double): Double {
        redisClient.sendCommand("HINCRBYFLOAT", key, value)
        return redisClient.receive()!!.value.toString().toDouble()
    }

    suspend fun getKeys(): List<String> {
        redisClient.sendCommand("HKEYS", key)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList()
    }

    suspend fun size(): Long {
        redisClient.sendCommand("HLEN", key)
        return redisClient.receive()!!.value as Long
    }

    suspend fun get(vararg keys: String): List<String> {
        redisClient.sendCommand("HMGET", key, *keys)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList()
    }

    suspend fun add(map: Map<String, String> = mapOf()) {
        redisClient.sendCommand("HMSET", map.flatMap { listOf(it.key, it.value) })
        redisClient.receive()
    }

    suspend fun put(key: String, value: String): Int {
        redisClient.sendCommand("HSET", this.key, key, value)
        return redisClient.receive()!!.value.fastCastTo<Long>().toInt()
    }

    suspend fun setNX(key: String, value: String): Boolean {
        redisClient.sendCommand("HSETNX", this.key, key, value)
        return redisClient.receive()!!.value == 1L
    }

}

fun RedisClient.getHash(key: String) = RedisMap(this, key)

suspend fun RedisClient.getMap(key: String) = getHash(key).get()

suspend fun RedisClient.putHash(key: String, map: Map<String, String> = mapOf()) = RedisMap(this, key).apply {
    if(map.isNotEmpty()) add(map)
}