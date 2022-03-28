package io.github.jan.rediskm.core.entities.collection

import io.github.jan.rediskm.core.entities.RedisElement
import io.github.jan.rediskm.core.entities.RedisObject

sealed interface RedisCollection <T> : RedisElement, RedisObject<T> {

    suspend fun size(): Long

    suspend fun contains(element: String): Boolean

}