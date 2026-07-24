1|package com.hermes.companion.ui.screenmodel
2|
3|import androidx.lifecycle.ViewModel
4|import kotlinx.coroutines.flow.MutableStateFlow
5|import kotlinx.coroutines.flow.StateFlow
6|import kotlinx.coroutines.flow.asStateFlow
7|import kotlinx.coroutines.flow.update
8|
9|class PerformanceViewModel : ViewModel() {
10|
11|    private val _uiState = MutableStateFlow(PerformanceUiState())
12|    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()
13|
14|    init {
15|        loadState()
16|    }
17|
18|    private fun loadState() {
19|        _uiState.update {
20|            it.copy(
21|                cpuUsage = 34f,
22|                ramUsage = 62f,
23|                ramTotal = 8f,
24|                ramUsed = 5f,
25|                batteryLevel = 78,
26|                isCharging = false,
27|                storageUsed = 64.2f,
28|                storageTotal = 128f,
29|                networkRx = 1.2f,
30|                networkTx = 0.8f,
31|                missionLatency = listOf(45f, 52f, 48f, 61f, 55f, 49f, 53f, 58f, 51f, 47f, 43f, 56f)
32|            )
33|        }
34|    }
35|
36|    fun refresh() {
37|        loadState()
38|    }
39|}