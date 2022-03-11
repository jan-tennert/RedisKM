package io.github.jan.rediskm.json.entities

import io.github.jan.rediskm.core.RedisClient

class RedisJsonPrimitive<T>(override val redisClient: RedisClient, override val key: String, override val path: String) : RedisJsonElement