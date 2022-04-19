package io.github.jan.rediskm.core.entities.collection

import com.soywiz.kds.fastCastTo
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.RedisException
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.utils.serialize
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.typeOf

class RedisSet internal constructor(override val redisClient: RedisClient, override val key: String): RedisCollection<Set<String>> {

    /**
     * Adds multiple values to the set.
     * @return the number of elements that were added to the set.
     */
    override suspend fun add(vararg elements: String): Long {
        redisClient.sendCommand("SADD", key, *elements)
        return redisClient.receive()!!.value as Long
    }

    /**
     * Adds multiple serialized values to the set.
     * @return the number of elements that were added to the set.
     */
    suspend inline fun <reified T> add(vararg elements: T) = add(*elements.map(::serialize).toTypedArray())

    override suspend fun size(): Long {
        redisClient.sendCommand("SCARD", key)
        return redisClient.receive()!!.value as Long
    }

    /**
     * Gets the difference between [otherSets] and this set and returns them in a new set.
     */
    suspend fun getDifference(vararg otherSets: String) = redisClient.getSetDifference(key, *otherSets)

    /**
     * Gets the difference between [otherSets] and this set and stores them in a new set.
     */
    suspend fun getDifferenceAndStore(destination: String, vararg otherSets: String) = redisClient.getSetDifferenceAndStore(destination, key, *otherSets)

    override suspend fun contains(element: String): Boolean {
        redisClient.sendCommand("SISMEMBER", key, element)
        return redisClient.receive()!!.value == 1L
    }

    override suspend fun get(): Set<String> {
        redisClient.sendCommand("SMEMBERS", key)
        return redisClient.receive()!!.value.fastCastTo<RedisListValue>().mapToStringList().toSet()
    }

    /**
     * Moves the [value] from the set to [destination].
     */
    suspend fun moveValue(value: String, destination: String): Boolean {
        redisClient.sendCommand("SMOVE", key, destination, value)
        return redisClient.receive()!!.value == 1L
    }

    /**
     * Gets a random value from the set and removes it
     */
    suspend fun getRandomAndRemove(): String {
        redisClient.sendCommand("SPOP", key)
        return redisClient.receive()!!.value.toString()
    }

    /**
     * Gets [count] amount of random values from the set
     */
    suspend fun getRandom(count: Int = 1): Set<String> {
        redisClient.sendCommand("SRANDMEMBER", key, count)
        return redisClient.receive()!!.value.fastCastTo<RedisListValue>().mapToStringList().toSet()
    }

    /**
     * Removes multiple values from the set.
     */
    suspend fun remove(vararg elements: String): Long {
        redisClient.sendCommand("SREM", key, *elements)
        return redisClient.receive()!!.value as Long
    }

    override suspend fun remove(element: String) = remove(elements = arrayOf(element))

    /**
     * Merges this set with [otherSets] and returns the result in a new set.
     */
    suspend fun mergeSets(vararg otherSets: String) = redisClient.mergeSets(key, *otherSets)

    /**
     * Merges this set with [otherSets] and stores the result in a new set.
     */
    suspend fun mergeSetsAndStore(destination: String, vararg otherSets: String) = redisClient.mergeSetsAndStore(destination, key, *otherSets)

    /**
     * Gets the intersection between this set and [otherSets] and returns the result in a new set.
     */
    suspend fun getIntersection(vararg otherSets: String) = redisClient.getSetIntersection(key, *otherSets)

    /**
     * Gets the intersection between this set and [otherSets] and stores the result in a new set.
     */
    suspend fun getIntersectionAndStore(destination: String, vararg otherSets: String) = redisClient.getSetIntersectionAndStore(destination, key, *otherSets)

}

suspend fun RedisClient.mergeSets(vararg otherSets: String): Set<String> {
    sendCommand("SUNION", *otherSets)
    return receive()!!.value.fastCastTo<RedisListValue>().mapToStringList().toSet()
}

suspend fun RedisClient.mergeSetsAndStore(destination: String, vararg otherSets: String) {
    sendCommand("SUNIONSTORE", destination, *otherSets)
}

suspend fun RedisClient.getSetIntersection(vararg sets: String): Set<String> {
    sendCommand("SINTER", *sets)
    return receive().fastCastTo<RedisListValue>().mapToStringList().toSet()
}

suspend fun RedisClient.getSetIntersectionAndStore(destination: String, vararg sets: String): Long {
    sendCommand("SINTERSTORE", destination, *sets)
    return receive()!!.value as Long
}

suspend fun RedisClient.getSetDifference(vararg sets: String): Set<String> {
    sendCommand("SDIFF", *sets)
    return receive().fastCastTo<RedisListValue>().mapToStringList().toSet()
}

suspend fun RedisClient.getSetDifferenceAndStore(destination: String, vararg sets: String): Long {
    sendCommand("SDIFFSTORE", destination, *sets)
    return receive()!!.value as Long
}

/**
 * Returns a [RedisSet] for the specified [key]. This doesn't make a call to the redis server until you call any function on this object.
 */
fun RedisClient.getSet(key: String) = RedisSet(this, key)

/**
 * Returns a serialized [Set] of [T] for the given [key]
 */
suspend inline fun <reified T> RedisClient.getSet(key: String) : Set<T> {
    val set = getSet(key).get()
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
 * Creates a new [RedisSet] with the given [key] and adds [values] to it.
 *
 * **Note**: If the set already exists, the values will be added to the end of the set
 * @return A [RedisSet] for the given [key]
 */
suspend inline fun <reified T> RedisClient.putSet(key: String, vararg values: T) : RedisSet {
    val set = getSet(key)
    set.add(*values)
    return set
}

/**
 * Creates a new [RedisSet] with the given [key] and adds [values] to it.
 *
 * **Note**: If the set already exists, the values will be added to the end of the set
 * @return A [RedisSet] for the given [key]
 */
suspend inline fun <reified T> RedisClient.putSet(key: String, builder: MutableSet<T>.() -> Unit) = putSet(key, buildSet(builder))