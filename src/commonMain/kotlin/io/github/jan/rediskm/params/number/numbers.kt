package io.github.jan.rediskm.params.number

import io.github.jan.rediskm.RedisClient
import io.github.jan.rediskm.readResponse

/**
 * Increase the integer value of a key
 */
suspend fun RedisClient.increase(key: String) = sendCommand("INCR", key).run {
    rawClient.readResponse() as Long
}

/**
 * Increase the integer value of a key by [value]
 */
suspend fun RedisClient.increaseBy(key: String, value: Long) = sendCommand("INCRBY", key, value.toString()).run {
    rawClient.readResponse() as Long
}

/**
 * Increase the float value of a key by [value]
 */
suspend fun RedisClient.increaseBy(key: String, value: Double) = sendCommand("INCRBYFLOAT", key, value.toString()).run {
    rawClient.readResponse().toString().toDouble()
}

/**
 * Decrease the integer value of a key
 */
suspend fun RedisClient.decrease(key: String) = sendCommand("DECR", key).run {
    rawClient.readResponse() as Long
}

/**
 * Decrease the integer value of a key by [value]
 */
suspend fun RedisClient.decreaseBy(key: String, value: Long) = sendCommand("DECRBY", key, value.toString()).run {
    rawClient.readResponse() as Long
}
