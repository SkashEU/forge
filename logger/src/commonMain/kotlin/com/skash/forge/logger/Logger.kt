package com.skash.forge.logger

import kotlin.jvm.JvmOverloads

enum class LogLevel {
    Debug,
    Info,
    Warn,
    Error
}

interface Logger {
    val tag: String

    fun processLog(
        level: LogLevel,
        tag: String,
        throwable: Throwable? = null,
        message: String
    )

    companion object
}

@JvmOverloads
inline fun Logger.d(throwable: Throwable? = null, tag: String = this.tag, message: () -> String) {
    processLog(LogLevel.Debug, tag = tag, message = message(), throwable = throwable)
}

@JvmOverloads
inline fun Logger.w(throwable: Throwable? = null, tag: String = this.tag, message: () -> String) {
    processLog(LogLevel.Warn, tag = tag, message = message(), throwable = throwable)

}

@JvmOverloads
inline fun Logger.i(tag: String = this.tag, message: () -> String) {
    processLog(LogLevel.Info, tag = tag, message = message())
}

@JvmOverloads
inline fun Logger.e(throwable: Throwable? = null, tag: String = this.tag, message: () -> String) {
    processLog(LogLevel.Error, tag = tag, message = message(), throwable = throwable)
}


