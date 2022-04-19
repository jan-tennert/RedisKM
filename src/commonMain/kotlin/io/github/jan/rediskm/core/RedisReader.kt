package io.github.jan.rediskm.core

import com.soywiz.korio.net.AsyncClient
import com.soywiz.korio.stream.readString
import io.github.jan.rediskm.core.entities.RedisDoubleValue
import io.github.jan.rediskm.core.entities.RedisIntegerValue
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.entities.RedisStringValue
import io.github.jan.rediskm.core.entities.RedisValue
suspend fun AsyncClient.readResponse(): RedisValue<*>? = when(val char = read().toChar()) {
    Constants.CHAR_PLUS -> {
        val value = readSimpleString()
        if(value.contains(".") && value.toDoubleOrNull() != null) {
            RedisDoubleValue(value.toDouble())
        } else if(value.toIntOrNull() != null) {
            RedisIntegerValue(value.toLong())
        } else {
            RedisStringValue(value)
        }
    }
    Constants.CHAR_DOLLAR -> {
        readRedisBulkString()?.let {
            if(it.contains(".") && it.toDoubleOrNull() != null) {
                RedisDoubleValue(it.toDouble())
            } else if(it.toIntOrNull() != null) {
                RedisIntegerValue(it.toLong())
            } else {
                RedisStringValue(it)
            }
        }
    }
    Constants.CHAR_COLON -> RedisIntegerValue(readNumber())
    Constants.CHAR_MINUS -> throw RedisException(readSimpleString())
    Constants.CHAR_STAR -> RedisListValue(readArray())
    else -> throw IllegalStateException("Unexpected char: $char")
}

private suspend fun AsyncClient.readSimpleString() = buildString {
    var value = read().toChar()
    while(value != '\r') {
        append(value)
        value = read().toChar()
    }
    value = read().toChar()
    if(value != '\n') throw IllegalStateException("Expected \\n at the end of a simple string")
}

private suspend fun AsyncClient.readRedisBulkString(): String? {
    val length = buildString {
        var value = read().toChar()
        while(value != '\r') {
            append(value)
            value = read().toChar()
        }
    }.toInt()
    if(read().toChar() != '\n') throw IllegalStateException("Expected \\r and \\n after the length of a bulk string")
    if(length == -1) return null
    val string = readString(length)
    if(read().toChar() != '\r' || read().toChar() != '\n') throw IllegalStateException("Expected \\r and \\n at the end of a bulk string")
    return string
}

private suspend fun AsyncClient.readArray() : List<RedisValue<*>?> {
    val length = buildString {
        var value = read().toChar()
        while(value != '\r') {
            append(value)
            value = read().toChar()
        }
    }.toInt()
    if(read().toChar() != '\n') throw IllegalStateException("Expected \\r and \\n after the length of an array")
    return buildList {
        for(i in 1..length) {
            add(readResponse())
        }
    }
}


private suspend fun AsyncClient.readNumber() = buildString {
    var value = read().toChar()
    while(value != '\r') {
        append(value)
        value = read().toChar()
    }
    if(read().toChar() != '\n') throw IllegalStateException("Expected \\r and \\n after the length of a bulk string")
}.toLong()