package com.hermes.companion.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class Converters {

    companion object {
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }

        @JvmStatic
        @TypeConverter
        fun missionStatusToString(status: com.hermes.companion.core.domain.MissionStatus): String = status.name

        @JvmStatic
        @TypeConverter
        fun stringToMissionStatus(s: String): com.hermes.companion.core.domain.MissionStatus =
            com.hermes.companion.core.domain.MissionStatus.valueOf(s)

        @JvmStatic
        @TypeConverter
        fun agentTypeToString(type: com.hermes.companion.core.domain.AgentType): String = type.name

        @JvmStatic
        @TypeConverter
        fun stringToAgentType(s: String): com.hermes.companion.core.domain.AgentType =
            com.hermes.companion.core.domain.AgentType.valueOf(s)

        @JvmStatic
        @TypeConverter
        fun agentStatusToString(status: com.hermes.companion.core.domain.AgentStatus): String = status.name

        @JvmStatic
        @TypeConverter
        fun stringToAgentStatus(s: String): com.hermes.companion.core.domain.AgentStatus =
            com.hermes.companion.core.domain.AgentStatus.valueOf(s)

        @JvmStatic
        @TypeConverter
        fun logLevelToString(level: com.hermes.companion.core.domain.LogLevel): String = level.name

        @JvmStatic
        @TypeConverter
        fun stringToLogLevel(s: String): com.hermes.companion.core.domain.LogLevel =
            com.hermes.companion.core.domain.LogLevel.valueOf(s)

        @JvmStatic
        @TypeConverter
        fun memoryTypeToString(type: com.hermes.companion.core.domain.MemoryType): String = type.name

        @JvmStatic
        @TypeConverter
        fun stringToMemoryType(s: String): com.hermes.companion.core.domain.MemoryType =
            com.hermes.companion.core.domain.MemoryType.valueOf(s)

        // List<String> ↔ JSON
        @JvmStatic
        @TypeConverter
        fun stringListToJson(list: List<String>): String = json.encodeToString(list)

        @JvmStatic
        @TypeConverter
        fun jsonToStringList(jsonStr: String): List<String> = json.decodeFromString(jsonStr)

        // List<AgentHistoryEntry> ↔ JSON
        @JvmStatic
        @TypeConverter
        fun historyListToJson(list: List<com.hermes.companion.core.domain.AgentHistoryEntry>): String =
            json.encodeToString(list)

        @JvmStatic
        @TypeConverter
        fun jsonToHistoryList(jsonStr: String): List<com.hermes.companion.core.domain.AgentHistoryEntry> =
            json.decodeFromString(jsonStr)

        // Map<String, String> ↔ JSON
        @JvmStatic
        @TypeConverter
        fun mapToJson(map: Map<String, String>): String = json.encodeToString(map)

        @JvmStatic
        @TypeConverter
        fun jsonToMap(jsonStr: String): Map<String, String> = json.decodeFromString(jsonStr)

        // List<Float> ↔ JSON
        @JvmStatic
        @TypeConverter
        fun floatListToJson(list: List<Float>): String = json.encodeToString(list)

        @JvmStatic
        @TypeConverter
        fun jsonToFloatList(jsonStr: String): List<Float> = json.decodeFromString(jsonStr)
    }
}