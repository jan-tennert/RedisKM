package io.github.jan.rediskm.core.logging

import com.soywiz.klock.DateTime
import com.soywiz.klogger.Logger

object LoggerOutput : Logger.Output {

    override fun output(logger: Logger, level: Logger.Level, msg: Any?) {
        if(logger.level.index < level.index) return
        output(msg, logger.name, level)
    }

    fun output(msg: Any?, name: String, level: Logger.Level = Logger.Level.INFO) {
        val message = when(level) {
            Logger.Level.NONE -> msg.toString()
            Logger.Level.FATAL -> ConsoleColors.RED_BRIGHT + msg.toString() + ConsoleColors.RESET
            Logger.Level.ERROR -> ConsoleColors.RED + msg.toString() + ConsoleColors.RESET
            Logger.Level.WARN -> ConsoleColors.YELLOW + msg.toString() + ConsoleColors.RESET
            Logger.Level.INFO -> msg.toString()
            Logger.Level.DEBUG -> msg.toString()
            Logger.Level.TRACE -> msg.toString()
        }
        val formattedTime = DateTime.nowLocal().toString("MM-dd-yyyy HH:mm:ssXXX")
        println("${ConsoleColors.CYAN}$formattedTime ${ConsoleColors.BLUE_BRIGHT + ("[${level.name}]") + ConsoleColors.RESET} ${ConsoleColors.GREEN_BRIGHT + ("(${name})") + ConsoleColors.RESET} $message")
    }

}

class LoggerConfig(var level: Logger.Level = Logger.Level.INFO, var output: Logger.Output = LoggerOutput) {

    operator fun invoke(builder: LoggerConfig.() -> Unit) {
        val config = LoggerConfig().apply(builder)
        this.level = config.level
        this.output = config.output
    }

}

object ConsoleColors {

    const val RED = "\u001b[0;31m"
    const val RED_BRIGHT = "\u001b[0;91m"
    const val YELLOW = "\u001b[0;33m"
    const val CYAN = "\u001b[0;36m"
    const val BLUE_BRIGHT = "\u001b[0;94m"
    const val GREEN_BRIGHT = "\u001b[0;92m"
    const val RESET = "\u001b[0m"

}

fun Logger.log(ignoreLevel: Boolean, level: Logger.Level = Logger.Level.INFO, msg: () -> Any?) {
    if(ignoreLevel) {
        output.output(this, level, msg())
    } else {
        log(level, msg)
    }
}