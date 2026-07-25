package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PerformanceUiState(
    val cpuUsage: Float = 0f,
    val ramUsage: Float = 0f,
    val ramTotal: Float = 8f,
    val ramUsed: Float = 0f,
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val storageUsed: Float = 0f,
    val storageTotal: Float = 0f,
    val networkRx: Float = 0f,
    val networkTx: Float = 0f,
    val missionLatency: List<Float> = emptyList()
)

class PerformanceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PerformanceUiState())
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    init { loadState() }

    private fun loadState() {
        _uiState.update {
            it.copy(
                cpuUsage = 34f,
                ramUsage = 62f,
                ramTotal = 8f,
                ramUsed = 5f,
                batteryLevel = 78,
                isCharging = false,
                storageUsed = 64.2f,
                storageTotal = 128f,
                networkRx = 1.2f,
                networkTx = 0.8f,
                missionLatency = listOf(45f, 52f, 48f, 61f, 55f, 49f, 53f, 58f, 51f, 47f, 43f, 56f)
            )
        }
    }

    fun refresh() { loadState() }
}
