package io.github.jan.rediskm

import com.soywiz.korio.lang.toByteArray
import com.soywiz.korio.net.AsyncClient

object RedisCommandType {

    const val AUTH = "AUTH"

}

data class RedisCommand(val args: List<Any>) {

    constructor(vararg args: Any) : this(args.toList())

    suspend fun write(client: AsyncClient) = RedisWriter.writeCommand(this, client)

}