package io.github.jan.rediskm.core.entities

interface RedisObject <T> {

    suspend fun get(): T

}