package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SensorData(
    val name: String,
    val type: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.Sensors
)

data class PermissionData(
    val name: String,
    val description: String,
    val granted: Boolean
)

data class AndroidControlUiState(
    val deviceModel: String = "Pixel 8 Pro",
    val androidVersion: String = "14",
    val apiLevel: Int = 34,
    val manufacturer: String = "Google",
    val batteryLevel: Int = 85,
    val isCharging: Boolean = false,
    val storageUsed: Float = 64.2f,
    val storageTotal: Float = 128f,
    val accessibilityEnabled: Boolean = false,
    val notificationServiceEnabled: Boolean = false,
    val mediaProjectionActive: Boolean = false,
    val sensors: List<SensorData> = emptyList(),
    val permissions: List<PermissionData> = emptyList()
)

class AndroidControlViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AndroidControlUiState())
    val uiState: StateFlow<AndroidControlUiState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        _uiState.update {
            it.copy(
                sensors = listOf(
                    SensorData("Accelerometer", "TYPE_ACCELEROMETER", "X: 0.02 Y: 0.01 Z: 9.81", Icons.Filled.Speed),
                    SensorData("Gyroscope", "TYPE_GYROSCOPE", "X: 0.00 Y: 0.00 Z: 0.00", Icons.Filled.Tune),
                    SensorData("Magnetometer", "TYPE_MAGNETIC_FIELD", "X: -12.5 Y: 28.3 Z: -45.1", Icons.Filled.Explore),
                    SensorData("Proximity", "TYPE_PROXIMITY", "0.0 cm (near)", Icons.Filled.CenterFocusStrong),
                    SensorData("Light", "TYPE_LIGHT", "128.5 lux", Icons.Filled.LightMode),
                    SensorData("Pressure", "TYPE_PRESSURE", "1013.25 hPa", Icons.Filled.Atmosphere)
                ),
                permissions = listOf(
                    PermissionData("CAMERA", "Take photos and record video", false),
                    PermissionData("RECORD_AUDIO", "Record audio", false),
                    PermissionData("ACCESS_FINE_LOCATION", "Precise location", true),
                    PermissionData("READ_EXTERNAL_STORAGE", "Read storage", true),
                    PermissionData("WRITE_EXTERNAL_STORAGE", "Write storage", true),
                    PermissionData("POST_NOTIFICATIONS", "Send notifications", true),
                    PermissionData("SYSTEM_ALERT_WINDOW", "Display over other apps", false)
                )
            )
        }
    }

    fun refresh() {
        loadState()
    }

    fun toggleAccessibility() {
        _uiState.update { it.copy(accessibilityEnabled = !it.accessibilityEnabled) }
    }

    fun toggleNotificationService() {
        _uiState.update { it.copy(notificationServiceEnabled = !it.notificationServiceEnabled) }
    }

    fun toggleMediaProjection() {
        _uiState.update { it.copy(mediaProjectionActive = !it.mediaProjectionActive) }
    }

    fun takeScreenshot() {
        // TODO: trigger screenshot via service
    }
}
