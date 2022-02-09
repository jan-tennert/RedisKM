package io.github.jan.rediskm

import com.soywiz.korio.lang.toByteArray
import com.soywiz.korio.net.AsyncClient
import com.soywiz.korio.stream.write32BE
import com.soywiz.korio.stream.write32LE
import com.soywiz.korio.stream.write64BE
import com.soywiz.korio.stream.write64LE
import com.soywiz.korio.stream.writeBytes
import com.soywiz.korio.stream.writeStringz

object RedisWriter {

    suspend fun writeCommand(command: RedisCommand, client: AsyncClient) {
        client.writeChar(Constants.CHAR_STAR)
        client.writeBytes(command.args.size.toString().encodeToByteArray())
        client.writeBytes(Constants.BYTE_CRLN)

        with(client) {
            command.args.forEach {
                when(it) {
                    is String -> writeString(it)
                   // is Long -> writeLong(it)
                  //  is Int -> writeInt(it)
                }
            }
        }
    }

    private suspend fun AsyncClient.writeChar(c: Char) = this.write(c.code)

    private fun AsyncClient.writeNull() {
     //   writeChar(Constants.CHAR_DOLLAR)

    }

    private suspend fun AsyncClient.writeInt(i: Int) {
        writeChar(Constants.CHAR_DOLLAR)
        writeBytes(numberToByteArray(i))
        writeBytes(Constants.BYTE_CRLN)
    }

    private suspend fun AsyncClient.writeLong(l: Long) {
        writeChar(Constants.CHAR_COLON)
        writeBytes(numberToByteArray(l, Long.SIZE_BYTES))
        writeBytes(Constants.BYTE_CRLN)
    }

    private suspend fun AsyncClient.writeString(s: String) {
        writeChar(Constants.CHAR_DOLLAR)
        writeBytes(numberToByteArray(s.length))
        writeBytes(Constants.BYTE_CRLN)
        writeBytes(s.toByteArray())
        writeBytes(Constants.BYTE_CRLN)
    }

    fun numberToByteArray (data: Number, size: Int = 4) : ByteArray =
        ByteArray (size) {i -> (data.toLong() shr (i*8)).toByte()}

}

