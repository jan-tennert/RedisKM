package io.github.jan.rediskm.core.entities

sealed interface RedisValue <T> {

    val value: T

}

data class RedisStringValue(override val value: String) : RedisValue<String> {

    override fun toString() = value

}

data class RedisIntegerValue(override val value: Long) : RedisValue<Long> {

    override fun toString() = value.toString()

}

data class RedisDoubleValue(override val value: Double) : RedisValue<Double> {

    override fun toString() = value.toString()

}

data class RedisListValue(override val value: List<RedisValue<*>?>) : RedisValue<List<RedisValue<*>?>>, List<RedisValue<*>?> by value {

    override fun toString() = value.toString()

    fun mapToStringList() = value.map(RedisValue<*>?::toString)

}