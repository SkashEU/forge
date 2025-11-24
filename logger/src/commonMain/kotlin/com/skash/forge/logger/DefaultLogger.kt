package com.skash.forge.logger

import co.touchlab.kermit.Logger as KermitLogger
import co.touchlab.kermit.Severity

object DefaultLogger : Logger {
    override val tag: String = "Forge"

    override fun processLog(
        level: LogLevel,
        tag: String,
        throwable: Throwable?,
        message: String
    ) {
        KermitLogger.processLog(
            severity = level.toKermitLogLevel(),
            tag = tag,
            throwable = throwable,
            message = message
        )
    }
}

private fun LogLevel.toKermitLogLevel(): Severity = when (this) {
    LogLevel.Debug -> Severity.Debug
    LogLevel.Info -> Severity.Info
    LogLevel.Warn -> Severity.Warn
    LogLevel.Error -> Severity.Error
}