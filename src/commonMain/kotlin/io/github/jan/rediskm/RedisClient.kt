package io.github.jan.rediskm

import com.soywiz.korio.async.launch
import com.soywiz.korio.net.AsyncClient
import com.soywiz.korio.stream.readStringz
import com.soywiz.korio.stream.writeBytes
import com.soywiz.korio.stream.writeString
import com.soywiz.korio.stream.writeStringz
import kotlinx.coroutines.coroutineScope

class RedisClient(private val host: String, private val port: Int, val username: String? = null, val password: String? = null, val secure: Boolean = false) {

    private lateinit var client: AsyncClient
    val isConnected: Boolean
        get() = ::client.isInitialized && client.connected

    suspend fun connect() {
        client = AsyncClient.createAndConnect(host, port, secure)
       // sendCommand(RedisCommand("KEYS *", listOf()))
    }

    suspend fun auth() = if (password != null) {
        println("hi")
        val command = RedisCommand(RedisCommandType.AUTH, if(username != null) listOf(username, password) else listOf(password))
       // println(command.build())
        sendCommand(command)
        println(RedisReader.readResponse(client))
        false
    } else false

    suspend fun sendCommand(command: RedisCommand) = command.write(client).also {
        println(RedisReader.readResponse(client))

    }

}