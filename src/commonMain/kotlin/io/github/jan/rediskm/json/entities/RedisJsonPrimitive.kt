package io.github.jan.rediskm.json.entities

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.json.params.appendString
import io.github.jan.rediskm.json.params.increaseNumberBy
import io.github.jan.rediskm.json.params.json
import io.github.jan.rediskm.json.params.multiplyNumberBy
import io.github.jan.rediskm.json.params.stringLength
import io.github.jan.rediskm.json.params.toggleBoolean

sealed interface RedisJsonPrimitive : RedisJsonElement

class RedisJsonString(override val redisClient: RedisClient, override val key: String, override val path: String) : RedisJsonElement, RedisJsonPrimitive

class RedisJsonNumber(override val redisClient: RedisClient, override val key: String, override val path: String) : RedisJsonElement, RedisJsonPrimitive

class RedisJsonBoolean(override val redisClient: RedisClient, override val key: String, override val path: String) : RedisJsonElement, RedisJsonPrimitive

inline fun <reified T : RedisJsonPrimitive> RedisJsonPrimitive.convertTo() = when(T::class) {
    RedisJsonString::class -> RedisJsonString(redisClient, key, path) as T
    RedisJsonNumber::class -> RedisJsonNumber(redisClient, key, path) as T
    RedisJsonBoolean::class -> RedisJsonBoolean(redisClient, key, path) as T
    else -> throw IllegalStateException("Unsupported json primitive")
}

suspend fun RedisJsonNumber.increaseBy(value: Double) = redisClient.json.increaseNumberBy(key, value, path)

suspend fun RedisJsonNumber.multiplyBy(value: Double) = redisClient.json.multiplyNumberBy(key, value, path)

suspend fun RedisJsonBoolean.toggle() = redisClient.json.toggleBoolean(key, path)

suspend fun RedisJsonString.append(value: String) = redisClient.json.appendString(key, value, path)

suspend fun RedisJsonString.length() = redisClient.json.stringLength(key, path)