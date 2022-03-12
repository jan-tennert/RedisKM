package io.github.jan.rediskm.json.entities

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.json.params.appendJson
import io.github.jan.rediskm.json.params.increaseByJson
import io.github.jan.rediskm.json.params.lengthJson
import io.github.jan.rediskm.json.params.multiplyByJson
import io.github.jan.rediskm.json.params.toggleJson

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

suspend fun RedisJsonNumber.increaseBy(value: Double) = redisClient.increaseByJson(key, value, path)

suspend fun RedisJsonNumber.multiplyBy(value: Double) = redisClient.multiplyByJson(key, value, path)

suspend fun RedisJsonBoolean.toggle() = redisClient.toggleJson(key, path)

suspend fun RedisJsonString.append(value: String) = redisClient.appendJson(key, value, path)

suspend fun RedisJsonString.length() = redisClient.lengthJson(key, path)