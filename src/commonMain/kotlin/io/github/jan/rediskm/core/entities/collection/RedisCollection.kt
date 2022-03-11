package io.github.jan.rediskm.core.entities.collection

sealed interface RedisCollection <T> {

    suspend fun size(): Long

    suspend fun contains(element: String): Boolean

}