1|package com.hermes.companion.ui.screenmodel
2|
3|import androidx.lifecycle.ViewModel
4|import androidx.compose.material.icons.Icons
5|import androidx.compose.material.icons.filled.*
6|import kotlinx.coroutines.flow.MutableStateFlow
7|import kotlinx.coroutines.flow.StateFlow
8|import kotlinx.coroutines.flow.asStateFlow
9|import kotlinx.coroutines.flow.update
10|
11|data class SensorData(
12|    val name: String,
13|    val type: String,
14|    val value: String,
15|    val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.Sensors
16|)
17|
18|data class PermissionData(
19|    val name: String,
20|    val description: String,
21|    val granted: Boolean
22|)
23|
24|data class AndroidControlUiState(
25|    val deviceModel: String = "Pixel 8 Pro",
26|    val androidVersion: String = "14",
27|    val apiLevel: Int = 34,
28|    val manufacturer: String = "Google",
29|    val batteryLevel: Int = 85,
30|    val isCharging: Boolean = false,
31|    val storageUsed: Float = 64.2f,
32|    val storageTotal: Float = 128f,
33|    val accessibilityEnabled: Boolean = false,
34|    val notificationServiceEnabled: Boolean = false,
35|    val mediaProjectionActive: Boolean = false,
36|    val sensors: List<SensorData> = emptyList(),
37|    val permissions: List<PermissionData> = emptyList()
38|)
39|
40|class AndroidControlViewModel : ViewModel() {
41|
42|    private val _uiState = MutableStateFlow(AndroidControlUiState())
43|    val uiState: StateFlow<AndroidControlUiState> = _uiState.asStateFlow()
44|
45|    init {
46|        loadState()
47|    }
48|
49|    private fun loadState() {
50|        _uiState.update {
51|            it.copy(
52|                sensors = listOf(
53|                    SensorData("Accelerometer", "TYPE_ACCELEROMETER", "X: 0.02 Y: 0.01 Z: 9.81", Icons.Filled.Speed),
54|                    SensorData("Gyroscope", "TYPE_GYROSCOPE", "X: 0.00 Y: 0.00 Z: 0.00", Icons.Filled.Tune),
55|                    SensorData("Magnetometer", "TYPE_MAGNETIC_FIELD", "X: -12.5 Y: 28.3 Z: -45.1", Icons.Filled.Explore),
56|                    SensorData("Proximity", "TYPE_PROXIMITY", "0.0 cm (near)", Icons.Filled.CenterFocusStrong),
57|                    SensorData("Light", "TYPE_LIGHT", "128.5 lux", Icons.Filled.LightMode),
58|                    SensorData("Pressure", "TYPE_PRESSURE", "1013.25 hPa", Icons.Filled.Atmosphere)
59|                ),
60|                permissions = listOf(
61|                    PermissionData("CAMERA", "Take photos and record video", false),
62|                    PermissionData("RECORD_AUDIO", "Record audio", false),
63|                    PermissionData("ACCESS_FINE_LOCATION", "Precise location", true),
64|                    PermissionData("READ_EXTERNAL_STORAGE", "Read storage", true),
65|                    PermissionData("WRITE_EXTERNAL_STORAGE", "Write storage", true),
66|                    PermissionData("POST_NOTIFICATIONS", "Send notifications", true),
67|                    PermissionData("SYSTEM_ALERT_WINDOW", "Display over other apps", false)
68|                )
69|            )
70|        }
71|    }
72|
73|    fun refresh() {
74|        loadState()
75|    }
76|
77|    fun toggleAccessibility() {
78|        _uiState.update { it.copy(accessibilityEnabled = !it.accessibilityEnabled) }
79|    }
80|
81|    fun toggleNotificationService() {
82|        _uiState.update { it.copy(notificationServiceEnabled = !it.notificationServiceEnabled) }
83|    }
84|
85|    fun toggleMediaProjection() {
86|        _uiState.update { it.copy(mediaProjectionActive = !it.mediaProjectionActive) }
87|    }
88|
89|    fun takeScreenshot() {
90|        // TODO: trigger screenshot via service
91|    }
92|}
93|