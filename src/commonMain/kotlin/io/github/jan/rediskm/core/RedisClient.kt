package io.github.jan.rediskm.core

import com.soywiz.korio.net.AsyncClient

/**
 * Creates a new Redis client.
 *
 * @param host The hostname of the Redis server.
 * @param port The port of the Redis server.
 * @param password The password used to authenticated with the Redis server.
 * @param username The username used to authenticated with the Redis server. (optional)
 * @param secure Whether to use SSL or not. (optional, only available for JVM, JS and Windows)
 */
class RedisClient(private val host: String, private val port: Int, val username: String? = null, val password: String, val secure: Boolean = false) {

    lateinit var rawClient: AsyncClient

    /**
     * Whether the client is connected to the server
     */
    val isConnected: Boolean
        get() = ::rawClient.isInitialized && rawClient.connected

    /**
     * Connects to the redis server
     *
     * @param auth Whether it should authenticate with the server after connecting. You can do that later with [authenticate]
     */
    suspend fun connect(auth: Boolean = true) {
        rawClient = AsyncClient.createAndConnect(host, port, secure)
        if(auth) authenticate()
    }

    /**
     * Authenticate to the server.
     *
     * @throws RedisException if authentication fails.
     */
    suspend fun authenticate() {
        val args = if(username != null) arrayOf(username, password) else arrayOf(password)
        val params = listOf("AUTH", *args).toTypedArray()
        sendCommand(*params)
        receive()
    }
    
    suspend fun receive() = rawClient.readResponse()

    suspend fun sendCommand(vararg args: Any) = rawClient.writeCommand(args.map(Any::toString))

}