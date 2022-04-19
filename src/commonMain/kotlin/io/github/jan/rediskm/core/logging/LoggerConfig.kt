package io.github.jan.rediskm.core.logging

import com.soywiz.klogger.Logger

sealed interface LoggerConfig {

    val level: Logger.Level
    val output: Logger.Output

    companion object : LoggerConfig {
        override val level = Logger.Level.INFO
        override val output: Logger.Output = LoggerOutput

        inline operator fun invoke(builder: LoggerConfigBuilder.() -> Unit) = LoggerConfigBuilder().apply(builder)
        operator fun invoke(level: Logger.Level = LoggerConfig.level, output: Logger.Output = LoggerConfig.output) = LoggerConfigBuilder(level, output)

    }

}

class LoggerConfigBuilder(override var level: Logger.Level = LoggerConfig.level, override var output: Logger.Output = LoggerConfig.output) : LoggerConfig