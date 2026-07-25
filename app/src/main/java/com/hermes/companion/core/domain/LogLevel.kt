package com.hermes.companion.core.domain

/**
 * Severity levels for log entries throughout the application.
 * Used by CompanionProtocol, LogRepository, LogsViewModel, LogsScreen, LogStream, etc.
 */
enum class LogLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    CRITICAL,
    FATAL
}