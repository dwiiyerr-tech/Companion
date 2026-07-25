package com.hermes.companion.data.repository

import com.hermes.companion.core.network.LogEvent
import com.hermes.companion.core.network.LogExport
import com.hermes.companion.core.network.LogFilter
import com.hermes.companion.core.domain.LogLevel
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileWriter

/**
 * Log storage, filtering, and export.
 *
 * Maintains a ring buffer of recent log entries. Supports real-time streaming,
 * filtering by level/source, and export to file.
 */
class LogRepository(
    private val hermesRepository: com.hermes.companion.data.repository.HermesRepository
) {
    companion object {
        private const val MAX_LOG_ENTRIES = 2000
    }

    // ── In-Memory Buffer ───────────────────────────────────

    private val _logs = MutableStateFlow<List<LogEvent>>(emptyList())
    val logs: StateFlow<List<LogEvent>> = _logs.asStateFlow()

    /** Real-time log stream (hot flow) */
    private val _logStream = MutableSharedFlow<LogEvent>(
        extraBufferCapacity = 256,
        onBufferOverflow = SharedFlow.OverflowStrategy.DROP_OLDEST
    )
    val logStream: SharedFlow<LogEvent> = _logStream.asSharedFlow()

    private val buffer = mutableListOf<LogEvent>()
    private var totalReceived: Long = 0L

    // ── Log Ingestion ──────────────────────────────────────

    /**
     * Add a log entry to the buffer.
     */
    fun addLog(event: LogEvent) {
        val entry = if (event.timestamp == 0L) {
            event.copy(timestamp = System.currentTimeMillis())
        } else {
            event
        }

        synchronized(buffer) {
            buffer.add(entry)
            totalReceived++
            if (buffer.size > MAX_LOG_ENTRIES) {
                buffer.removeFirst()
            }
            _logs.value = buffer.toList()
        }

        // Emit to stream (non-blocking)
        _logStream.tryEmit(entry)
    }

    /**
     * Add a log with convenience parameters.
     */
    fun addLog(level: LogLevel, source: String, message: String, data: Map<String, String> = emptyMap()) {
        addLog(LogEvent(
            level = level,
            source = source,
            message = message,
            data = data,
            timestamp = System.currentTimeMillis()
        ))
    }

    /**
     * Process a log event from Brain WebSocket.
     */
    fun processBrainLog(event: LogEvent) {
        addLog(event)
    }

    // ── Query ──────────────────────────────────────────────

    /**
     * Get all logs.
     */
    fun getAllLogs(): List<LogEvent> = buffer.toList()

    /**
     * Get logs filtered by a LogFilter.
     */
    fun getFiltered(filter: LogFilter): List<LogEvent> {
        return synchronized(buffer) {
            buffer.filter { event ->
                val matchesLevel = filter.levels.isEmpty() || event.level in filter.levels
                val matchesSource = filter.sources.isEmpty() || event.source in filter.sources
                val matchesQuery = filter.query.isNullOrBlank() ||
                    event.message.contains(filter.query, ignoreCase = true)
                val matchesTime = (filter.startTime == null || event.timestamp >= filter.startTime) &&
                    (filter.endTime == null || event.timestamp <= filter.endTime)
                matchesLevel && matchesSource && matchesQuery && matchesTime
            }.sortedByDescending { it.timestamp }
            .drop(filter.offset)
            .take(filter.limit)
        }
    }

    /**
     * Get logs by level.
     */
    fun getByLevel(level: LogLevel): List<LogEvent> {
        return buffer.filter { it.level == level }.sortedByDescending { it.timestamp }
    }

    /**
     * Get logs by source.
     */
    fun getBySource(source: String): List<LogEvent> {
        return buffer.filter { it.source == source }.sortedByDescending { it.timestamp }
    }

    /**
     * Get recent logs (last N).
     */
    fun getRecent(count: Int = 50): List<LogEvent> {
        return buffer.takeLast(count).sortedByDescending { it.timestamp }
    }

    /**
     * Get error logs only.
     */
    fun getErrors(): List<LogEvent> {
        return buffer.filter { it.level == LogLevel.ERROR || it.level == LogLevel.FATAL }
            .sortedByDescending { it.timestamp }
    }

    /**
     * Get log count by level.
     */
    fun getCountByLevel(level: LogLevel): Int {
        return buffer.count { it.level == level }
    }

    /**
     * Get total log count.
     */
    fun getTotalCount(): Long = totalReceived

    /**
     * Get unique log sources.
     */
    fun getSources(): List<String> {
        return buffer.map { it.source }.distinct().sorted()
    }

    // ── Export ─────────────────────────────────────────────

    /**
     * Export logs to a file.
     */
    suspend fun exportLogs(
        exportConfig: LogExport,
        outputFile: File
    ): Result<Unit> {
        return try {
            val logs = if (exportConfig.filter != null) {
                getFiltered(exportConfig.filter)
            } else {
                buffer.toList()
            }

            when (exportConfig.format) {
                com.hermes.companion.core.network.ExportFormat.JSON -> {
                    val json = kotlinx.serialization.json.Json.encodeToString(
                        kotlinx.serialization.builtins.ListSerializer(LogEvent.serializer()),
                        logs
                    )
                    outputFile.writeText(json)
                }
                com.hermes.companion.core.network.ExportFormat.CSV -> {
                    FileWriter(outputFile).use { writer ->
                        writer.write("timestamp,level,source,message\n")
                        logs.forEach { event ->
                            writer.write("${event.timestamp},${event.level},${event.source},\"${event.message.replace("\"", "\"\"")}\"\n")
                        }
                    }
                }
                com.hermes.companion.core.network.ExportFormat.PLAINTEXT -> {
                    FileWriter(outputFile).use { writer ->
                        logs.forEach { event ->
                            writer.write("[${event.timestamp}] [${event.level}] [${event.source}] ${event.message}\n")
                        }
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Cleanup ────────────────────────────────────────────

    /**
     * Clear all logs.
     */
    fun clear() {
        synchronized(buffer) {
            buffer.clear()
            _logs.value = emptyList()
            totalReceived = 0L
        }
    }

    /**
     * Trim buffer to a specific size.
     */
    fun trimBuffer(maxSize: Int = MAX_LOG_ENTRIES) {
        synchronized(buffer) {
            while (buffer.size > maxSize) {
                buffer.removeFirst()
            }
            _logs.value = buffer.toList()
        }
    }
}
