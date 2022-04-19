package io.github.jan.rediskm.core.entities.collection

import com.soywiz.kds.fastCastTo
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.RedisException
import io.github.jan.rediskm.core.entities.RedisElement
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.entities.RedisObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.typeOf

class RedisSortedSet internal constructor(override val redisClient: RedisClient, override val key: String) : RedisObject<Set<String>>, RedisElement {

    override suspend fun get() = subSet(0, -1)

    suspend fun contains(element: String) = indexOf(element) != -1L

    suspend fun size(): Long {
        redisClient.sendCommand("ZCARD", key)
        return redisClient.receive()!!.value.fastCastTo()
    }

    /**
     * Adds multiple elements to the sorted set
     */
    suspend fun add(items: Map<Double, String>): Long {
        redisClient.sendCommand("ZADD", key, *items.flatMap { listOf(it.key, it.value) }.toTypedArray())
        return redisClient.receive()!!.value.fastCastTo()
    }

    /**
     * Returns the number of elements between the scores [min] and [max]
     */
    suspend fun scoresBetween(min: Long, max: Long): Long {
        redisClient.sendCommand("ZCOUNT", key, min, max)
        return redisClient.receive()!!.value.fastCastTo()
    }

    /**
     * Increase the score of [member] by [value]
     */
    suspend fun increaseScoreBy(member: String, value: Long): Double {
        redisClient.sendCommand("ZINCRBY", key, value, member)
        return redisClient.receive()!!.value.toString().toDouble()
    }

    /**
     * Gets the intersection of this sorted set with other [sortedSets] and stores them in [destination]
     */
    suspend fun getIntersectionAndStore(destination: String, vararg sortedSets: String, extraArgs: List<String> = emptyList()) = redisClient.getSortedSetIntersectionAndStore(destination, *sortedSets, extraArgs = extraArgs)

    suspend fun getLexCount(min: String, max: String): Long {
        redisClient.sendCommand("ZLEXCOUNT", key, min, max)
        return redisClient.receive()!!.value.fastCastTo()
    }

    /**
     * Returns a sub list with the items from the list between [start] and [end]
     *
     * **Note**: This operation does not edit the original list. Use [trim] if you want to edit the original list
     * @return The sub list
     */
    suspend fun subSet(start: Long, end: Long): Set<String> {
        redisClient.sendCommand("ZRANGE", key, start, end)
        return (redisClient.receive() as RedisListValue).mapToStringList().toSet()
    }

    /**
     * Returns a sub list with the items from the list between [start] and [end] with their scores
     *
     * **Note**: This operation does not edit the original list. Use [trim] if you want to edit the original list
     * @return The sub list
     */
    suspend fun subListWithScores(start: Long, end: Long): Map<Double, String> {
        redisClient.sendCommand("ZRANGE", key, start, end, "WITHSCORES")
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList().chunked(2) { (key, value) -> key.toDouble() to value }.toMap()
    }

    suspend fun rangeByLex(min: String, max: String, limit: Long = -1, offset: Long = 0): List<String> {
        redisClient.sendCommand("ZRANGEBYLEX", key, min, max, limit, offset)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList()
    }

    /**
     * Returns elements with the scores between [min] and [max] with a limit of [limit]
     */
    suspend fun rangeByScore(min: Long, max: Long, limit: Long = -1, offset: Long = 0): List<String> {
        redisClient.sendCommand("ZRANGEBYSCORE", key, min, max, limit, offset)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList()
    }

    /**
     * Returns elements with the scores between [min] and [max] with a limit of [limit] with their scores
     */
    suspend fun rangeByScoreWithScores(min: Long, max: Long, limit: Long = -1, offset: Long = 0): Map<Double, String> {
        redisClient.sendCommand("ZRANGEBYSCORE", key, min, max, limit, offset, "WITHSCORES")
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList().chunked(2) { (key, value) -> key.toDouble() to value }.toMap()
    }

    /**
     * Gets the index of [member]
     */
    suspend fun indexOf(member: String): Long {
        redisClient.sendCommand("ZRANK", key, member)
        return (redisClient.receive()?.value as? Long) ?: -1L
    }

    /**
     * Removes multiple elements from the sorted set
     */
    suspend fun remove(vararg members: String): Long {
        redisClient.sendCommand("ZREM", key, *members)
        return redisClient.receive()!!.value.toString().toLong()
    }

    /**
     * Removes elements multiple elements between with the index [min] and [max]
     */
    suspend fun removeRange(start: Long, end: Long): Long {
        redisClient.sendCommand("ZREMRANGEBYRANK", key, start, end)
        return redisClient.receive()!!.value.toString().toLong()
    }

    /**
     * Removes elements multiple elements between with the score [min] and [max]
     */
    suspend fun removeRangeByScore(min: Long, max: Long): Long {
        redisClient.sendCommand("ZREMRANGEBYSCORE", key, min, max)
        return redisClient.receive()!!.value.toString().toLong()
    }

    /**
     * Returns the score of [member]
     */
    suspend fun scoreOf(member: String): Double {
        redisClient.sendCommand("ZSCORE", key, member)
        return redisClient.receive()!!.value.toString().toDouble()
    }

    /**
     * Merges other [sortedSets] and this sorted set and stores them in [destination]
     */
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

/**
 * Returns a [RedisSortedSet] for the specified [key]. This doesn't make a call to the redis server until you call any function on this object.
 */
fun RedisClient.getSortedSet(key: String) = RedisSortedSet(this, key)

/**
 * Returns a serialized [Set] of [T] for the given [key]
 */
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

/**
 * Creates a new [RedisSortedSet] with the given [key] and adds [values] to it.
 *
 * **Note**: If the set already exists, the values will be added to the end of the set
 * @return A [RedisSortedSet] for the given [key]
 */
suspend inline fun RedisClient.putSortedSet(key: String, items: Map<Double, String>) : RedisSortedSet{
    val set = getSortedSet(key)
    set.add(items)
    return set
}

/**
 * Creates a new [RedisSortedSet] with the given [key] and adds [values] to it.
 *
 * **Note**: If the set already exists, the values will be added to the end of the set
 * @return A [RedisSortedSet] for the given [key]
 */
suspend inline fun RedisClient.putSortedSet(key: String, builder: MutableMap<Double, String>.() -> Unit) = putSortedSet(key, buildMap(builder))