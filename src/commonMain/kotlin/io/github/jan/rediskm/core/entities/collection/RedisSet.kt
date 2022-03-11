package io.github.jan.rediskm.core.entities.collection

import com.soywiz.kds.fastCastTo
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.entities.RedisObject
import io.github.jan.rediskm.core.utils.serialize

class RedisSet(val redisClient: RedisClient, val key: String): RedisObject<Set<String>>, RedisCollection<String> {

    suspend fun add(vararg elements: String): Long {
        redisClient.sendCommand("SADD", key, *elements)
        return redisClient.receive()!!.value as Long
    }

    suspend inline fun <reified T> add(vararg elements: T) = add(*elements.map(::serialize).toTypedArray())

    override suspend fun size(): Long {
        redisClient.sendCommand("SCARD", key)
        return redisClient.receive()!!.value as Long
    }

    suspend fun getDifference(vararg otherSets: String) = redisClient.getDifference(key, *otherSets)

    suspend fun getDifferenceAndStore(destination: String, vararg otherSets: String) = redisClient.getDifferenceAndStore(destination, key, *otherSets)

    override suspend fun contains(element: String): Boolean {
        redisClient.sendCommand("SISMEMBER", key, element)
        return redisClient.receive()!!.value == 1L
    }

    override suspend fun get(): Set<String> {
        redisClient.sendCommand("SMEMBERS", key)
        return redisClient.receive()!!.value.fastCastTo<RedisListValue>().toSet()
    }

    suspend fun moveValue(value: String, destination: String): Boolean {
        redisClient.sendCommand("SMOVE", key, destination, value)
        return redisClient.receive()!!.value == 1L
    }

    suspend fun getRandomAndRemove(): String {
        redisClient.sendCommand("SPOP", key)
        return redisClient.receive()!!.value.toString()
    }

    suspend fun getRandom(count: Int = 1): Set<String> {
        redisClient.sendCommand("SRANDMEMBER", key, count)
        return redisClient.receive()!!.value.fastCastTo<RedisListValue>().toSet()
    }

    suspend fun remove(vararg elements: String): Long {
        redisClient.sendCommand("SREM", key, *elements)
        return redisClient.receive()!!.value as Long
    }

    suspend fun mergeSets(vararg otherSets: String) = redisClient.mergeSets(key, *otherSets)

    suspend fun mergeSetsAndStore(destination: String, vararg otherSets: String) = redisClient.mergeSetsAndStore(destination, key, *otherSets)

}

suspend fun RedisClient.mergeSets(vararg otherSets: String): Set<String> {
    sendCommand("SUNION", *otherSets)
    return receive()!!.value.fastCastTo<RedisListValue>().toSet()
}

suspend fun RedisClient.mergeSetsAndStore(destination: String, vararg otherSets: String) {
    sendCommand("SUNIONSTORE", destination, *otherSets)
}

suspend fun RedisClient.getIntersection(vararg sets: String): Set<String> {
    sendCommand("SINTER", *sets)
    return receive().fastCastTo<RedisListValue>().toSet()
}

suspend fun RedisClient.getIntersectionAndStore(destination: String, vararg sets: String): Long {
    sendCommand("SINTERSTORE", destination, *sets)
    return receive()!!.value as Long
}

suspend fun RedisClient.getDifference(vararg sets: String): Set<String> {
    sendCommand("SDIFF", *sets)
    return receive().fastCastTo<RedisListValue>().toSet()
}

suspend fun RedisClient.getDifferenceAndStore(destination: String, vararg sets: String): Long {
    sendCommand("SDIFFSTORE", destination, *sets)
    return receive()!!.value as Long
}