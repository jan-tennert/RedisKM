package io.github.jan.rediskm.core.entities

sealed interface RedisValue <T> {

    val value: T

}

data class RedisStringValue(override val value: String) : RedisValue<String>
data class RedisIntegerValue(override val value: Long) : RedisValue<Long>
data class RedisListValue(override val value: List<String>) : RedisValue<List<String>>, Iterable<String> by value