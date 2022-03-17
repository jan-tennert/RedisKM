package io.github.jan.rediskm.core.entities.collection

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.RedisException
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.entities.RedisObject
import io.github.jan.rediskm.core.utils.deserialize
import io.github.jan.rediskm.core.utils.serialize
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.typeOf

class RedisList(val redisClient: RedisClient, val key: String) : RedisObject<RedisListValue>, RedisCollection<String> {

    override suspend fun get() = subList(0, -1)

    suspend fun popFirst(timeout: TimeSpan = 0.seconds): RedisListValue {
        redisClient.sendCommand("BLPOP", key, timeout.seconds.toInt())
        return redisClient.receive() as RedisListValue
    }

    suspend fun popLast(timeout: TimeSpan = 0.seconds): RedisListValue {
        redisClient.sendCommand("BRPOP", key, timeout.seconds.toInt())
        return redisClient.receive() as RedisListValue
    }

    suspend fun popAndPush(destination: String): RedisListValue {
        redisClient.sendCommand("BRPOPLPUSH", key, destination)
        return redisClient.receive() as RedisListValue
    }

    suspend inline fun <reified T> get(index: Int): T {
        redisClient.sendCommand("LINDEX", key, index)
        return deserialize(redisClient.receive()!!)
    }

    suspend fun insert(value: String, before: String): Long {
        redisClient.sendCommand("LINSERT", key, "BEFORE", before, value)
        return redisClient.receive()!!.value as Long
    }

    override suspend fun size(): Long {
        redisClient.sendCommand("LLEN", key)
        return redisClient.receive()!!.value as Long
    }

    suspend fun add(vararg elements: String): Long {
        redisClient.sendCommand("LPUSH", key, *elements)
        return redisClient.receive()!!.value as Long
    }

    suspend inline fun <reified T> add(vararg elements: T): Long {
        redisClient.sendCommand("LPUSH", key, *elements.map(::serialize).toTypedArray())
        return redisClient.receive()!!.value as Long
    }

    suspend inline fun <reified T> addX(vararg elements: T): Long {
        redisClient.sendCommand("LPUSHX", key, *elements.map(::serialize).toTypedArray())
        return redisClient.receive()!!.value as Long
    }

    suspend fun subList(start: Int, end: Int): RedisListValue {
        redisClient.sendCommand("LRANGE", key, start, end)
        return redisClient.receive() as RedisListValue
    }

    suspend fun remove(value: String, count: Int = 0): Long {
        redisClient.sendCommand("LREM", key, count, value)
        return redisClient.receive()!!.value as Long
    }

    suspend fun set(index: Int, value: String) {
        redisClient.sendCommand("LSET", key, index, value)
        redisClient.receive()
    }

    suspend fun trim(start: Int, end: Int) {
        redisClient.sendCommand("LTRIM", key, start, end)
        redisClient.receive()
    }

    suspend fun addLast(vararg values: String): Long {
        redisClient.sendCommand("RPUSH", key, *values)
        return redisClient.receive()!!.value as Long
    }

    suspend fun addLastX(vararg values: String): Long {
        redisClient.sendCommand("RPUSHX", key, *values)
        return redisClient.receive()!!.value as Long
    }

    override suspend fun contains(element: String) = get().contains(element)

}

suspend inline fun <reified T> RedisClient.getList(key: String) : List<T> {
    val list = getList(key).get()
    return when(T::class) {
        String::class -> list.map { it as T }
        Int::class -> list.map { it.toInt() as T }
        Long::class -> list.map { it.toLong() as T }
        Float::class -> list.map { it.toFloat() as T }
        Double::class -> list.map { it.toDouble() as T }
        else -> {
            val serializer = serializerOrNull(typeOf<T>()) ?: throw RedisException("Unsupported type: ${T::class}")
            list.map { Json.decodeFromString(serializer, it) as T }
        }
    }
}

fun RedisClient.getList(key: String) = RedisList(this, key)

suspend inline fun <reified T> RedisClient.putList(key: String, vararg values: T) {
    val list = getList(key)
    list.add(*values)
}