package io.github.jan.rediskm

import com.soywiz.kmem.arraycopy
import com.soywiz.korio.net.AsyncClient
import com.soywiz.korio.stream.readStringz


object RedisReader {

    private const val DEFAULT_BUFFER_SIZE = 1024

    suspend fun readResponse(client: AsyncClient) : Any? {
        return when(client.read().toChar()) {
            Constants.CHAR_PLUS -> client.readStringz()
            Constants.CHAR_DOLLAR -> client.readRedisBulkString()
            Constants.CHAR_COLON -> client.readNumber()
            Constants.CHAR_MINUS -> client.readStringz()
            else -> throw IllegalStateException("")
        }
    }

    private suspend fun AsyncClient.readRedisBulkString(): String? {
        val ch: Int = readNumber().toInt()
        if (ch == -1) {
            return null
        }
        val byteArr = ByteArray(ch)
        read(byteArr, 0, 0)
        if (read() != '\r'.code) {
        //    throw RedisException("CR  was expected")
        }
        if (read() != '\n'.code) {
          //  throw RedisException("LR was expected")
        }
        return byteArr.decodeToString()
    }

    private suspend fun AsyncClient.readNumber() = readStringz().toLong()

}