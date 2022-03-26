package io.github.jan.rediskm.core.entities.collection

import com.soywiz.kds.fastCastTo
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.RedisException
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.entities.RedisObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.typeOf

class RedisSortedSet internal constructor(val redisClient: RedisClient, val key: String) : RedisCollection<String>, RedisObject<List<String>> {

    override suspend fun get() = subList(0, -1)

    override suspend fun contains(element: String) = indexOf(element) != -1L

    override suspend fun size(): Long {
        redisClient.sendCommand("ZCARD", key)
        return redisClient.receive()!!.value.fastCastTo()
    }

    suspend fun add(items: Map<Long, String>): Long {
        redisClient.sendCommand("ZADD", key, *items.flatMap { listOf(it.key, it.value) }.toTypedArray())
        return redisClient.receive()!!.value.fastCastTo()
    }

    suspend fun scoresBetween(min: Long, max: Long): Long {
        redisClient.sendCommand("ZCOUNT", key, min, max)
        return redisClient.receive()!!.value.fastCastTo()
    }

    suspend fun increaseScoreBy(member: String, value: Long): Long {
        redisClient.sendCommand("ZINCRBY", key, value, member)
        return redisClient.receive()!!.value.toString().toLong()
    }

    suspend fun getIntersectionAndStore(destination: String, vararg sortedSets: String, extraArgs: List<String> = emptyList()) = redisClient.getSortedSetIntersectionAndStore(destination, *sortedSets, extraArgs = extraArgs)

    suspend fun getLexCount(min: String, max: String): Long {
        redisClient.sendCommand("ZLEXCOUNT", key, min, max)
        return redisClient.receive()!!.value.fastCastTo()
    }

    suspend fun subList(start: Long, end: Long): List<String> {
        redisClient.sendCommand("ZRANGE", key, start, end)
        return (redisClient.receive() as RedisListValue).mapToStringList()
    }

    suspend fun subListWithScores(start: Long, end: Long): Map<Long, String> {
        redisClient.sendCommand("ZRANGE", key, start, end, "WITHSCORES")
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList().chunked(2) { (key, value) -> key.toLong() to value }.toMap()
    }

    suspend fun rangeByLex(min: String, max: String, limit: Long = -1, offset: Long = 0): List<String> {
        redisClient.sendCommand("ZRANGEBYLEX", key, min, max, limit, offset)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList()
    }

    suspend fun rangeByScore(min: Long, max: Long, limit: Long = -1, offset: Long = 0): List<String> {
        redisClient.sendCommand("ZRANGEBYSCORE", key, min, max, limit, offset)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList()
    }

    suspend fun rangeByScoreWithScores(min: Long, max: Long, limit: Long = -1, offset: Long = 0): Map<Long, String> {
        redisClient.sendCommand("ZRANGEBYSCORE", key, min, max, limit, offset, "WITHSCORES")
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList().chunked(2) { (key, value) -> key.toLong() to value }.toMap()
    }

    suspend fun indexOf(member: String): Long {
        redisClient.sendCommand("ZRANK", key, member)
        return (redisClient.receive()?.value as? Long) ?: -1L
    }

    suspend fun remove(vararg members: String): Long {
        redisClient.sendCommand("ZREM", key, *members)
        return redisClient.receive()!!.value.toString().toLong()
    }

    suspend fun removeRange(start: Long, end: Long): Long {
        redisClient.sendCommand("ZREMRANGEBYRANK", key, start, end)
        return redisClient.receive()!!.value.toString().toLong()
    }

    suspend fun removeRangeByScore(min: Long, max: Long): Long {
        redisClient.sendCommand("ZREMRANGEBYSCORE", key, min, max)
        return redisClient.receive()!!.value.toString().toLong()
    }

    suspend fun scoreOf(member: String): Long {
        redisClient.sendCommand("ZSCORE", key, member)
        return redisClient.receive()!!.value.toString().toLong()
    }

    suspend fun mergeAndStore(destination: String, vararg sortedSets: String, extraArgs: List<String> = emptyList()) = redisClient.mergeSortedSetsAndStore(destination, *sortedSets, extraArgs = extraArgs)

}

suspend fun RedisClient.mergeSortedSetsAndStore(destination: String, vararg sortedSets: String, extraArgs: List<String> = emptyList()) {
    sendCommand("ZUNIONSTORE", destination, sortedSets.size, *sortedSets, *extraArgs.toTypedArray())
    receive()
}

suspend fun RedisClient.getSortedSetIntersectionAndStore(destination: String, vararg sortedSets: String, extraArgs: List<String> = emptyList()): Long {
    sendCommand("ZINTERSTORE", destination, sortedSets.size.toString(), *sortedSets, *extraArgs.toTypedArray())
    return receive()!!.value.fastCastTo()
}

fun RedisClient.getSortedSet(key: String) = RedisSortedSet(this, key)

suspend inline fun <reified T> RedisClient.getSortedSet(key: String) : Set<T> {
    val set = getSortedSet(key).get()
    return when(T::class) {
        String::class -> set.map { it as T }.toSet()
        Int::class -> set.map { it.toInt() as T }.toSet()
        Long::class -> set.map { it.toLong() as T }.toSet()
        Float::class -> set.map { it.toFloat() as T }.toSet()
        Double::class -> set.map { it.toDouble() as T }.toSet()
        else -> {
            val serializer = serializerOrNull(typeOf<T>()) ?: throw RedisException("Unsupported type: ${T::class}")
            set.map { Json.decodeFromString(serializer, it) as T }.toSet()
        }
    }
}

suspend inline fun RedisClient.putSortedSet(key: String, items: Map<Long, String>) {
    val set = getSortedSet(key)
    set.add(items)
}