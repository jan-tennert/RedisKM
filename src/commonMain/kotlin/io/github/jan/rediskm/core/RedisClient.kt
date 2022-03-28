package io.github.jan.rediskm.core

import com.soywiz.klock.milliseconds
import com.soywiz.klogger.Logger
import com.soywiz.korio.async.delay
import com.soywiz.korio.net.AsyncClient
import io.github.jan.rediskm.core.annotiations.RedisKMInternal
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.entities.RedisValue
import io.github.jan.rediskm.core.entities.pipeline.RedisPipeline
import io.github.jan.rediskm.core.entities.pipeline.RedisPipelineImpl
import io.github.jan.rediskm.core.entities.pipeline.RedisTransaction
import io.github.jan.rediskm.core.entities.pipeline.RedisTransactionImpl
import io.github.jan.rediskm.core.logging.LoggerConfig
import io.github.jan.rediskm.core.logging.log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface RedisClient {

    val host: String
    val username: String?
    val password: String
    val isSecure: Boolean
    val isConnected: Boolean
    val modules: MutableMap<String, RedisModule>
    val pipeline: RedisPipeline

    @RedisKMInternal
    val rawClient: AsyncClient

    suspend fun connect(auth: Boolean = true)

    suspend fun disconnect()

    suspend fun authenticate()

    suspend fun receive(): RedisValue<*>?

    suspend fun sendCommand(vararg args: Any)

    suspend fun createTransaction(usePipeline: Boolean = true): RedisTransaction

    suspend fun createTransaction(usePipeline: Boolean = true, init: suspend RedisTransaction.() -> Unit): RedisListValue

    companion object {

        fun create(host: String, port: Int, password: String, username: String? = null, isSecure: Boolean = false, loggerConfig: LoggerConfig = LoggerConfig()): RedisClient {
            return RedisClientImpl(host, port, username, password, isSecure, loggerConfig)
        }

        suspend fun createAndConnect(host: String, port: Int, password: String, username: String? = null, isSecure: Boolean = false, loggerConfig: LoggerConfig = LoggerConfig()): RedisClient {
            return RedisClientImpl(host, port, username, password, isSecure, loggerConfig).apply {
                connect()
            }
        }

    }

}

internal class RedisClientImpl(override val host: String, private val port: Int, override val username: String? = null, override val password: String, override val isSecure: Boolean = false, loggerConfig: LoggerConfig = LoggerConfig()) : RedisClient {

    @OptIn(RedisKMInternal::class)
    override lateinit var rawClient: AsyncClient
    override val modules = mutableMapOf<String, RedisModule>()
    private val mutex = Mutex()
    override val pipeline: RedisPipeline = RedisPipelineImpl(this)
    val LOGGER = Logger("RedisClient")
    var locked = false

    init {
        LOGGER.level = loggerConfig.level
        LOGGER.output = loggerConfig.output
    }

    /**
     * Whether the client is connected to the server
     */
    override val isConnected: Boolean
        get() = ::rawClient.isInitialized && rawClient.connected

    /**
     * Connects to the redis server
     *
     * @param auth Whether it should authenticate with the server after connecting. You can do that later with [authenticate]
     */
    override suspend fun connect(auth: Boolean) {
        LOGGER.log(true, Logger.Level.INFO) {
            "Connecting to $host:$port"
        }
        try {
            rawClient = AsyncClient.createAndConnect(host, port, isSecure)
        } catch(_: Exception) {
            throw RedisException("Failed to connect to $host:$port")
        }
        LOGGER.log(true, Logger.Level.INFO) {
            "Successfully connected to to $host:$port!"
        }
        if(auth) authenticate()
    }

    /**
     * Disconnects from the redis server
     */
    override suspend fun disconnect() {
        sendCommand("QUIT")
        receive()
        rawClient.close()
    }
    /**
     * Authenticate to the server.
     *
     * @throws RedisException if authentication fails.
     */
    override suspend fun authenticate() {
        LOGGER.log(true, Logger.Level.INFO) {
            "Authenticating..."
        }
        val args = if(username != null) arrayOf(username, password) else arrayOf(password)
        val params = listOf("AUTH", *args).toTypedArray()
        sendCommand(*params)
        receive()
        LOGGER.log(true, Logger.Level.INFO) {
            "Successfully authenticated!"
        }
    }
    
    override suspend fun receive(): RedisValue<*>? {
        return if(locked) {
            while(locked) {
                delay(1.milliseconds)
            }
            receive()
        } else {
            mutex.withLock { locked = true }
            rawClient.readResponse().also {
                mutex.withLock { locked = false }
                LOGGER.debug {
                    "Received response: $it"
                }
            }
        }
    }

    override suspend fun sendCommand(vararg args: Any) {
        LOGGER.debug {
            "Sending command (args): ${args.joinToString(" ")}"
        }
        rawClient.writeCommand(args.map(Any::toString))
    }

    override suspend fun createTransaction(usePipeline: Boolean): RedisTransaction {
        sendCommand("MULTI")
        receive()
        return RedisTransactionImpl(this, usePipeline)
    }

    override suspend fun createTransaction(usePipeline: Boolean, init: suspend RedisTransaction.() -> Unit): RedisListValue {
        val transaction = createTransaction(usePipeline)
        init(transaction)
        return transaction.executeAll()
    }

}
