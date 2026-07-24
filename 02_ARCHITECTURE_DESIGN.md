# Hermes Android Companion - Detailed Architecture Design

## Core Architecture Overview

The Hermes Android Companion is a production-ready Android Operating System dashboard for managing the Hermes AI OS. It follows Hermes's established design patterns while introducing native Android-specific implementations.

```
┌─────────────────────────────────────────────────────────────┐
│                 Android Companion (Kotlin + Compose)       │
├─────────────────────────────────────────────────────────────┤
│ Layer 1: UI Layer (Jetpack Compose)                        │
│ Layer 2: ViewModel & State Management                     │
│ Layer 3: Data Access & Repositories                       │
│ Layer 4: Core Services & Networking                      │
│ Layer 5: Android System Integration                      │
│ Layer 6: Hermes Brain Connection                        │
└─────────────────────────────────────────────────────────────┘
```

## Layer 1: UI Layer (Compose Architecture)

### Navigation System
```kotlin
object HermesNavigation {
    // Main entry point
    const val LAUNCHER_ACTIVITY = "hermes://home"
    
    // Bottom navigation (6 core modules)
    object BottomTabs {
        const val HOME = "home"
        const val MISSION = "mission"
        const val AGENTS = "agents"
        const val ANDROID_RUNTIME = "android_runtime"
        const val BROWSER = "browser"
        const val MEMORY = "memory"
    }
    
    // Top-level routes with deep linking
    const val HOME = "hermes://home/main"
    const val MISSION_CENTER = "hermes://home/mission"
    const val MISSION_FLOW = "hermes://home/mission/flow/{missionId}"
    const val AGENT_DETAIL = "hermes://home/agents/{agentId}"
    const val ANDROID_CONTROL = "hermes://home/android_control/system"
    const val BROWSER_CDP = "hermes://home/browser/cdp"
    const val MEMORY_VIEWER = "hermes://home/memory/viewer/{type}"
    const val TOOLS = "hermes://home/tools/registry"
    const val PLUGINS = "hermes://home/plugins/marketplace"
    const val LOGS = "hermes://home/logs/live"
    const val PERFORMANCE = "hermes://home/performance/dashboard"
    const val SETTINGS = "hermes://home/settings/general"
    const val DEVELOPER = "hermes://home/developer/mode"
}
```

### Floating Overlay Architecture
```kotlin
data class FloatingOverlayState(
    val mode: OverlayMode = OverlayMode.BUBBLE,
    val windowToken: String? = null,
    val isVisible: Boolean = false,
    val voiceListening: Boolean = false,
    val missionRunning: Boolean = false,
    val thinking: Boolean = false,
    val error: ErrorState? = null
)

enum class OverlayMode {
    BUBBLE,          // Small circular icon
    COMPACT,         // Small bar with quick actions  
    EXPANDED,        // Full widget view
    FULLSCREEN       // Dialog takeover
}
```

## Layer 2: ViewModel & State Management

### MVVM with StateFlow Architecture
```kotlin
// Core ViewModels
class HermesViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(
                hermesRepository, agentRepository, memoryRepository
            ) as T
            modelClass.isAssignableFrom(MissionCenterViewModel::class.java) -> MissionCenterViewModel(
                hermesRepository, missionRepository
            ) as T
            // ... other ViewModels
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// HomeViewModel - Main dashboard state
class HomeViewModel(
    private val hermesRepository: HermesRepository,
    private val agentRepository: AgentRepository,
    private val memoryRepository: MemoryRepository
) : ViewModel() {
    
    // Brain Connection State
    private val _brainConnectionState = MutableStateFlow[BrainConnectionState] (BrainConnectionState.DISCONNECTED)
    val brainConnectionState: StateFlow<BrainConnectionState> = _brainConnectionState.asStateFlow()
    
    // Mission Overview State  
    private val _missionOverview = MutableStateFlow<List<MissionSummary>>(emptyList())
    val missionOverview: StateFlow<List<MissionSummary>> = _missionOverview.asStateFlow()
    
    // Agent Health State
    private val _agentHealth = MutableStateFlow<Map<String, AgentHealth>>(emptyMap())
    val agentHealth: StateFlow<Map<String, AgentHealth>> = _agentHealth.asStateFlow()
    
    // System Metrics State
    private val _systemMetrics = MutableStateFlow<SystemMetrics>(SystemMetrics())
    val systemMetrics: StateFlow<SystemMetrics> = _systemMetrics.asStateFlow()
    
    // Quick Actions State
    private val _quickActions = MutableStateFlow<List<QuickAction>>(emptyList())
    val quickActions: StateFlow<List<QuickAction>> = _quickActions.asStateFlow()
    
    init {
        loadDashboardData()
        observeBrainEvents()
        observeAgentUpdates()
    }
    
    private fun observeBrainEvents() {
        lifecycleScope.launch {
            hermesRepository.eventFlow.collect { event ->
                when (event.type) {
                    "brain.status" -> handleBrainStatus(event)
                    "mission.update" -> handleMissionUpdate(event)  
                    "agent.health" -> handleAgentHealth(event)
                }
            }
        }
    }
}
```

## Layer 3: Data Access & Repositories

### Repository Pattern with Offline Support
```kotlin
// HermesRepository - Brain communication layer
interface HermesRepository {
    suspend fun connectToBrain(host: String, port: Int, authToken: String): Result<ConnectionInfo>
    suspend fun disconnect(): Result<Unit>
    suspend fun sendCommand(command: HermesCommand): Result<CommandResponse>
    suspend fun createMission(mission: MissionRequest): Result<String>
    suspend fun queryMemory(query: MemoryQuery): Result<MemoryResult>
    fun observeEvents(): Flow<HermesEvent>
    suspend fun getBrainStatus(): Result<BrainStatus>
}

// Room Database Setup for offline persistence
@Database(entities = [Mission::class, Agent::class, Log::class, Memory::class], version = 1)
abstract class HermesDatabase : RoomDatabase() {
    abstract fun missionDao(): MissionDao
    abstract fun agentDao(): AgentDao
    abstract fun logDao(): LogDao
    abstract fun memoryDao(): MemoryDao
}
```

## Layer 4: Core Services & Networking

### Secure WebSocket Client
```kotlin
class SecureWebSocketClient(
    private val context: Context,
    private val eventDispatcher: EventDispatcher
) {
    private var webSocket: OkHttpWebSocket? = null
    private val connectionState = MutableStateFlow<WebSocketState>(WebSocketState.DISCONNECTED)
    val state: StateFlow<WebSocketState> = connectionState.asStateFlow()
    
    suspend fun connect(url: String, authToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $authToken")
                .addHeader("User-Agent", "Hermes-Android-Companion/${BuildConfig.VERSION_CODE}")
                .build()
            
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket) {
                    this@WebSocketClient.webSocket = webSocket
                    connectionState.value = WebSocketState.CONNECTED
                    eventDispatcher.dispatch(Event.BrainConnected)
                    
                    // Start heartbeat
                    startHeartbeat()
                }
                
                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleIncomingMessage(text)
                }
                
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosing(webSocket, code, reason)
                    connectionState.value = WebSocketState.CONNECTING
                }
                
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    connectionState.value = WebSocketState.DISCONNECTED
                    eventDispatcher.dispatch(Event.BrainConnectionFailed(t))
                    // Auto-reconnect
                    scheduleReconnect()
                }
            }
            
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
            
            val ws = client.newWebSocket(request, listener)
            this@WebSocketClient.webSocket = ws
            
            // Send initial handshake
            val handshake = mapOf("type" to "handshake", "client" to "android_companion")
            webSocket.send(Gson().toJson(handshake))
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun startHeartbeat() {
        lifecycleScope.launch {
            while (isActive && connectionState.value == WebSocketState.CONNECTED) {
                delay(HEARTBEAT_INTERVAL_MS)
                webSocket?.send("${HEARTBEAT_TYPE}")
            }
        }
    }
}
```

## Layer 5: Android System Integration

### System Service Integration
```kotlin
// HermesSystemBridge - All Android system integrations
class HermesSystemBridge(private val context: Context) {
    
    // Accessibility Service Integration
    class HermesAccessibilityService : AccessibilityService() {
        override fun onServiceConnected() {
            super.onServiceConnected()
            viewModel.onAccessibilityConnected()
        }
        
        override fun onAccessibilityEvent(event: AccessibilityEvent?) {
            event?.let {
                viewModel.onAccessibilityEvent(it)
            }
        }
    }
    
    // Notification Service Integration  
    class HermesNotificationListenerService : NotificationListenerService() {
        override fun onListenerConnected() {
            super.onListenerConnected()
            viewModel.onNotificationsConnected()
        }
        
        override fun onNotificationPosted(sbn: StatusBarNotification?) {
            sbn?.let {
                viewModel.onNotificationPosted(it)
            }
        }
    }
    
    // MediaProjection Service Integration
    class HermesMediaProjectionService : Service() {
        override fun onCreate() {
            super.onCreate()
            viewModel.onMediaProjectionCreated()
        }
        
        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            // Handle screenshot requests
            intent?.let {
                if (it.hasExtra("capture")) {
                    viewModel.captureScreenshot()
                }
            }
            return START_STICKY
        }
    }
    
    // Device Administration
    class HermesDeviceAdmin : DeviceAdminReceiver() {
        override fun onEnabled(context: Context, intent: Intent) {
            super.onEnabled(context, intent)
            // Enable additional system controls
        }
        
        override fun onDisabled(context: Context, intent: Intent) {
            super.onDisabled(context, intent)
            // Remove enhanced permissions
        }
    }
}
```

## Layer 6: Hermes Brain Connection

### Connection Manager with Resilience
```kotlin
class BrainConnectionManager(
    context: Context,
    private val eventDispatcher: EventDispatcher
) {
    
    private val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val state: StateFlow<ConnectionState> = connectionState.asStateFlow()
    
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private val reconnectDelay = 5000L // 5 seconds
    
    fun connect(brainHost: String, authToken: String) {
        lifecycleScope.launch {
            try {
                connectionState.value = ConnectionState.CONNECTING
                
                // Validate brain host
                if (!validateBrainAddress(brainHost)) {
                    connectionState.value = ConnectionState.FAILED("Invalid brain address")
                    return@launch
                }
                
                // Attempt WebSocket connection
                val webSocketClient = SecureWebSocketClient(context, eventDispatcher)
                val result = webSocketClient.connect(
                    "ws://$brainHost:9876",
                    authToken
                )
                
                if (result.isSuccess) {
                    connectionState.value = ConnectionState.CONNECTED
                    reconnectAttempts = 0
                    eventDispatcher.dispatch(Event.BrainConnected(result.getOrNull()))
                } else {
                    throw result.exceptionOrNull() ?: Exception("Connection failed")
                }
                
            } catch (e: Exception) {
                reconnectAttempts++
                if (reconnectAttempts < maxReconnectAttempts) {
                    delay(reconnectDelay * reconnectAttempts)
                    connect(brainHost, authToken) // Recursive retry
                } else {
                    connectionState.value = ConnectionState.FAILED(e.message ?: "Max retries exceeded")
                    eventDispatcher.dispatch(Event.BrainConnectionFailed(e))
                }
            }
        }
    }
    
    fun disconnect() {
        lifecycleScope.launch {
            connectionState.value = ConnectionState.DISCONNECTING
            // Cleanup resources
            eventDispatcher.dispatch(Event.BrainDisconnected)
            connectionState.value = ConnectionState.DISCONNECTED
        }
    }
}
```

## Module-Specific Architecture

### 1. Launcher Activity
```kotlin
class HermesLauncherActivity : ComponentActivity() {
    
    private val viewModel: LauncherViewModel by viewModels()
    private lateinit var binding: ActivityLauncherBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Deep link handling
        handleDeepLink(intent)
        
        // Floating overlay setup
        setupFloatingOverlay()
        
        // Permission manager
        setupPermissions()
        
        // Observe ViewModel state
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }
    
    private fun handleDeepLink(intent: Intent) {
        when {
            intent.data?.host == "mission" -> {
                val missionId = intent.data?.getQueryParameter("id")
                if (missionId != null) {
                    viewModel.openMission(missionId)
                }
            }
            intent.data?.host == "agent" -> {
                val agentId = intent.data?.getQueryParameter("id")
                if (agentId != null) {
                    viewModel.openAgent(agentId)
                }
            }
        }
    }
}
```

### 2. Mission Center Module
```kotlin
// MissionCenterViewModel - Handles all mission lifecycle
class MissionCenterViewModel(
    private val hermesRepository: HermesRepository,
    private val missionRepository: MissionRepository
) : ViewModel() {
    
    private val _runningMissions = MutableStateFlow<List<Mission>>(emptyList())
    val runningMissions: StateFlow<List<Mission>> = _runningMissions.asStateFlow()
    
    private val _missionHistory = MutableStateFlow<List<Mission>>(emptyList())
    val missionHistory: StateFlow<List<Mission>> = _missionHistory.asStateFlow()
    
    private val _missionQueues = MutableStateFlow<List<MissionQueue>>(emptyList())
    val missionQueues: StateFlow<List<MissionQueue>> = _missionQueues.asStateFlow()
    
    init {
        loadMissionData()
        observeMissionUpdates()
    }
    
    private fun observeMissionUpdates() {
        lifecycleScope.launch {
            hermesRepository.eventFlow
                .filter { it.type == "mission.update" || it.type == "mission.complete" }
                .collect { event ->
                    when (event.type) {
                        "mission.update" -> handleMissionUpdateEvent(event)
                        "mission.complete" -> handleMissionCompleteEvent(event)
                    }
                }
        }
    }
    
    fun createMission(missionName: String, goals: List<String>) {
        lifecycleScope.launch {
            val missionRequest = MissionRequest(
                name = missionName,
                goals = goals,
                agentTypes = listOf("plannerexecutor", "browser", "android"),
                priority = MissionPriority.HIGH
            )
            
            val result = hermesRepository.createMission(missionRequest)
            result.fold(
                onSuccess = { missionId ->
                    _runningMissions.value = _runningMissions.value + Mission(
                        id = missionId,
                        name = missionName,
                        status = MissionStatus.RUNNING,
                        createdAt = System.currentTimeMillis()
                    )
                },
                onFailure = { error ->
                    // Handle error
                }
            )
        }
    }
    
    fun pauseMission(missionId: String) {
        viewModelScope.launch {
            hermesRepository.sendCommand(
                HermesCommand.PauseMission(missionId)
            )
        }
    }
    
    fun resumeMission(missionId: String) {
        viewModelScope.launch {
            hermesRepository.sendCommand(
                HermesCommand.ResumeMission(missionId)
            )
        }
    }
    
    fun cancelMission(missionId: String) {
        viewModelScope.launch {
            hermesRepository.sendCommand(
                HermesCommand.CancelMission(missionId)
            )
            // Remove from running missions
            _runningMissions.value = _runningMissions.value.filter { it.id != missionId }
        }
    }
}
```

### 3. Agent Center Module
```kotlin
// AgentCenterViewModel - Agent monitoring and management
class AgentCenterViewModel(
    private val hermesRepository: HermesRepository,
    private val agentRepository: AgentRepository
) : ViewModel() {
    
    private val _agents = MutableStateFlow<List<Agent>>(emptyList())
    val agents: StateFlow<List<Agent>> = _agents.asStateFlow()
    
    private val _agentStats = MutableStateFlow<AgentStatistics>(AgentStatistics())
    val agentStats: StateFlow<AgentStatistics> = _agentStats.asStateFlow()
    
    private val _activeTasks = MutableStateFlow<List<ActiveTask>>(emptyList())
    val activeTasks: StateFlow<List<ActiveTask>> = _activeTasks.asStateFlow()
    
    init {
        observeAgentUpdates()
        loadAgentData()
    }
    
    private fun observeAgentUpdates() {
        lifecycleScope.launch {
            hermesRepository.eventFlow
                .filter { it.type == "agent.health" || it.type == "agent.task.update" }
                .collect { event ->
                    when (event.type) {
                        "agent.health" -> updateAgentHealth(event)
                        "agent.task.update" -> updateAgentTask(event)
                    }
                }
        }
    }
    
    fun requestAgentRefresh(agentId: String) {
        viewModelScope.launch {
            hermesRepository.sendCommand(HermesCommand.RefreshAgent(agentId))
        }
    }
    
    fun triggerAgentAction(agentId: String, action: AgentAction) {
        viewModelScope.launch {
            hermesRepository.sendCommand(
                HermesCommand.ExecuteAgentAction(agentId, action)
            )
        }
    }
}
```

### 4. Android Runtime Control Module
```kotlin
// AndroidRuntimeViewModel - System control and monitoring
class AndroidRuntimeViewModel(
    private val systemBridge: HermesSystemBridge,
    private val hermesRepository: HermesRepository
) : ViewModel() {
    
    private val _systemStatus = MutableStateFlow<SystemStatus>(SystemStatus())
    val systemStatus: StateFlow<SystemStatus> = _systemStatus.asStateFlow()
    
    private val _runtimeServices = MutableStateFlow<List<RuntimeService>>(emptyList())
    val runtimeServices: StateFlow<List<RuntimeService>> = _runtimeServices.asStateFlow()
    
    private val _sensors = MutableStateFlow<List<SensorData>>(emptyList())
    val sensors: StateFlow<List<SensorData>> = _sensors.asStateFlow()
    
    init {
        observeSystemUpdates()
        loadSystemStatus()
    }
    
    private fun observeSystemUpdates() {
        lifecycleScope.launch {
            hermesRepository.eventFlow
                .filter { it.type == "system.status" || it.type == "sensor.data" }
                .collect { event ->
                    when (event.type) {
                        "system.status" -> updateSystemStatus(event)
                        "sensor.data" -> updateSensorData(event)
                    }
                }
        }
    }
    
    // Accessibility Control
    fun enableAccessibilityService() {
        viewModelScope.launch {
            systemBridge.accessibilityService?.enable() ?: run {
                // Start accessibility service
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }
    
    fun toggleNotificationAccess() {
        viewModelScope.launch {
            systemBridge.notificationService?.toggleAccess() ?: run {
                // Open notification listener settings
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }
    
    // Media Projection Control
    fun requestScreenshot() {
        viewModelScope.launch {
            systemBridge.mediaProjectionService?.captureScreenshot()
                ?.collect { screenshot ->
                    // Handle screenshot result
                    _systemStatus.value = _systemStatus.value.copy(lastScreenshot = screenshot)
                }
        }
    }
    
    // Device Information
    fun getDeviceInfo() {
        val info = systemBridge.getDeviceInfo()
        _systemStatus.value = _systemStatus.value.copy(deviceInfo = info)
    }
}
```

## Communication Patterns

### Event-Driven Architecture
```kotlin
// Event types for inter-module communication
sealed class HermesEvent {
    data class BrainStatusChanged(val status: BrainStatus) : HermesEvent()
    data class MissionStateChanged(val mission: Mission) : HermesEvent()
    data class AgentHealthUpdated(val agentId: String, val health: AgentHealth) : HermesEvent()
    data class SystemError(val error: Throwable) : HermesEvent()
    data class MessageReceived(val message: HermesMessage) : HermesEvent()
}

// Event Dispatcher for cross-module communication
class EventDispatcher {
    private val eventFlow = MutableSharedFlow<HermesEvent>(
        extraBufferCapacity = 100,
        onBufferOverflow = SharedFlow.OverflowStrategy.DROP_OLDEST
    )
    
    fun dispatch(event: HermesEvent) {
        lifecycleScope.launch {
            eventFlow.emit(event)
        }
    }
    
    fun createEventFlow(): Flow<HermesEvent> = eventFlow.asSharedFlow()
}
```

## State Management Patterns

### Cross-Module State Synchronization
```kotlin
// Global AppState to coordinate across all modules
data class AppState(
    val brainConnection: BrainConnectionState = BrainConnectionState.DISCONNECTED,
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastSync: Long = 0,
    val offlineMode: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "brainConnection" to brainConnection,
        "currentUser" to currentUser,
        "isLoading" to isLoading,
        "error" to error,
        "lastSync" to lastSync,
        "offlineMode" to offlineMode
    )
}
```

## Performance Optimizations

### Multi-Process Architecture
```kotlin
// Use multiple processes for heavy operations
class HermesProcesses {
    companion object {
        const val BRAIN_PROCESS = "hermes.brain"
        const val SYSTEM_PROCESS = "hermes.system"
        const val UI_PROCESS = "hermes.ui"
    }
    
    // Usage in AndroidManifest.xml
    // <service android:name=".processes.BrainProcessService" android:process=":hermes.brain"/>
    // <service android:name=".processes.SystemProcessService" android:process=":hermes.system"/>
}
```

## Security Architecture

### Permission Management
```kotlin
class PermissionManager {
    private val requiredPermissions = listOf(
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
        Manifest.permission.SYSTEM_ALERT_WINDOW
    )
    
    fun checkAllPermissions(context: Context): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun requestPermissions(fragment: Fragment) {
        fragment.requestPermissions(
            requiredPermissions.toTypedArray(),
            PERMISSIONS_REQUEST_CODE
        )
    }
}
```

## Testing Architecture

### Dependency Injection for Testing
```kotlin
// Koin configuration for testing
object HermesTestModules {
    fun testModules() = listOf(
        // Mock repositories for unit testing
        factory { MockHermesRepository() },
        factory { MockAgentRepository() },
        factory { MockMemoryRepository() },
        // Test-specific services
        factory { TestWebSocketClient() },
        factory { TestSystemBridge() },
        // ViewModel factories
        factory { HomeViewModel(it[MockHermesRepository::class], it[MockAgentRepository::class]) },
        factory { MissionCenterViewModel(it[MockHermesRepository::class], it[MockSystemBridge::class]) }
    )
}
```

## Deployment Architecture

### Application Lifecycle Management
```kotlin
class HermesApplication : Application() {
    companion object {
        lateinit var hermesComponent: HermesComponent
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin
        hermesComponent = Koin_android_init(this) {
            modules(
                listOf(
                    // Core modules
                    viewModelModule,
                    // Network modules  
                    networkModule,
                    // Android-specific modules
                    androidSystemModule,
                    // Test modules (conditional)
                    *if (BuildConfig.IS_TEST) testModules().toTypedArray() else arrayOf()
                )
            )
        }
        
        // Register crash reporting
        Crashlytics.getInstance().core.enablePeriodicReports()
        
        // Initialize analytics
        Analytics.initialize(this, BuildConfig.ANALYTICS_KEY)
        
        // Setup background tasks
        setupBackgroundServices()
    }
    
    private fun setupBackgroundServices() {
        // Start Hermes web socket service
        val intent = Intent(this, HermesWebSocketService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        // Setup periodic backups
        val backupJob = WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "hermes_backup",
                ExistingPeriodicWorkPolicy.KEEP,
                OneTimeWorkRequest.Builder(HermesBackupWorker::class).build()
            )
    }
}
```

This comprehensive architecture provides a solid foundation for the Hermes Android Companion, ensuring it meets the complex requirements of managing the Hermes AI OS while maintaining production-level reliability and user experience.