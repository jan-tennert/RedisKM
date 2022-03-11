package io.github.jan.rediskm.core.params.number

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisElement

/**
 * Increase the integer value of a key
 */
suspend fun RedisElement.increase() = run {
    redisClient.sendCommand("INCR", key)
    redisClient.receive()!!.value as Long
}

/**
 * Increase the integer value of a key by [value]
 */
suspend fun RedisElement.increaseBy(value: Long) = run {
    redisClient.sendCommand("INCRBY", key, value.toString())
    redisClient.receive()!!.value as Long
}

/**
 * Increase the float value of a key by [value]
 */
suspend fun RedisElement.increaseBy(value: Double) = run {
    redisClient.sendCommand("INCRBYFLOAT", key, value.toString())
    redisClient.receive()!!.value.toString().toDouble()
}

/**
 * Decrease the integer value of a key
 */
suspend fun RedisElement.decrease() = run {
    redisClient.sendCommand("DECR", key)
    redisClient.receive()!!.value as Long
}

/**
 * Decrease the integer value of a key by [value]
 */
suspend fun RedisElement.decreaseBy(value: Long) = run {
    redisClient.sendCommand("DECRBY", key, value.toString())
    redisClient.receive()!!.value as Long
}
